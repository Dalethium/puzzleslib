package fuzs.puzzleslib.impl.client.core;

import com.google.common.collect.Lists;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.DynamicBakingCompletedContext;
import fuzs.puzzleslib.api.client.core.v1.context.DynamicModifyBakingResultContext;
import fuzs.puzzleslib.api.core.v1.ContentRegistrationFlags;
import fuzs.puzzleslib.api.core.v1.ModContainerHelper;
import fuzs.puzzleslib.impl.PuzzlesLib;
import fuzs.puzzleslib.impl.client.core.context.*;
import fuzs.puzzleslib.impl.client.core.event.RegisterItemDecorationsEvent;
import fuzs.puzzleslib.impl.core.context.AddReloadListenersContextForgeImpl;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class ForgeClientModConstructor {

    private ForgeClientModConstructor() {

    }


    public static void construct(ClientModConstructor constructor, String modId, Set<ContentRegistrationFlags> availableFlags, Set<ContentRegistrationFlags> flagsToHandle) {
        ModContainerHelper.findModEventBus(modId).ifPresent(eventBus -> {
            registerModHandlers(constructor, modId, eventBus, Lists.newArrayList(), availableFlags);
            constructor.onConstructMod();
        });
    }

    private static void registerModHandlers(ClientModConstructor constructor, String modId, IEventBus eventBus, List<ResourceManagerReloadListener> dynamicRenderers, Set<ContentRegistrationFlags> availableFlags) {
        eventBus.addListener((final FMLClientSetupEvent evt) -> {
            evt.enqueueWork(() -> {
                constructor.onClientSetup();
                constructor.onRegisterSearchTrees(new SearchRegistryContextForgeImpl());
                constructor.onRegisterItemModelProperties(new ItemModelPropertiesContextForgeImpl());
                constructor.onRegisterBuiltinModelItemRenderers(new BuiltinModelItemRendererContextForgeImpl(dynamicRenderers));
                constructor.onRegisterBlockRenderTypes(new BlockRenderTypesContextForgeImpl());
                constructor.onRegisterFluidRenderTypes(new FluidRenderTypesContextForgeImpl());
                constructor.onRegisterClientTooltipComponents(new ClientTooltipComponentsContextForgeImpl());
                constructor.onRegisterKeyMappings(new KeyMappingsContextForgeImpl());
            });
        });
        eventBus.addListener((final EntityRenderersEvent.RegisterRenderers evt) -> {
            constructor.onRegisterEntityRenderers(new EntityRenderersContextForgeImpl(evt::registerEntityRenderer));
            constructor.onRegisterBlockEntityRenderers(new BlockEntityRenderersContextForgeImpl(evt::registerBlockEntityRenderer));
        });
        eventBus.addListener((final ParticleFactoryRegisterEvent evt) -> {
            constructor.onRegisterParticleProviders(new ParticleProvidersContextForgeImpl());
        });
        eventBus.addListener((final TextureStitchEvent.Pre evt) -> {
            constructor.onRegisterAtlasSprites(new AtlasSpritesContextForgeImpl(evt.getAtlas(), evt::addSprite));
        });
        eventBus.addListener((final EntityRenderersEvent.RegisterLayerDefinitions evt) -> {
            constructor.onRegisterLayerDefinitions(new LayerDefinitionsContextForgeImpl(evt::registerLayerDefinition));
        });
        eventBus.addListener((final ModelBakeEvent evt) -> {
            onModifyBakingResult(constructor::onModifyBakingResult, modId, evt.getModelRegistry(), evt.getModelLoader());
            onBakingCompleted(constructor::onBakingCompleted, modId, evt.getModelManager(), Collections.unmodifiableMap(evt.getModelRegistry()), evt.getModelLoader());
        });
        eventBus.addListener((final ModelRegistryEvent evt) -> {
            constructor.onRegisterAdditionalModels(new AdditionalModelsContextForgeImpl());
        });
        eventBus.addListener((final RegisterItemDecorationsEvent evt) -> {
            constructor.onRegisterItemDecorations(new ItemDecorationContextForgeImpl(evt::register));
        });
//        eventBus.addListener((final RegisterEntitySpectatorShadersEvent evt) -> {
//            constructor.onRegisterEntitySpectatorShaders(new EntitySpectatorShaderContextForgeImpl(evt::register));
//        });
        eventBus.addListener((final EntityRenderersEvent.CreateSkullModels evt) -> {
            constructor.onRegisterSkullRenderers(new SkullRenderersContextForgeImpl(evt.getEntityModelSet(), evt::registerSkullModel));
        });
        eventBus.addListener((final RegisterClientReloadListenersEvent evt) -> {
            constructor.onRegisterResourcePackReloadListeners(new AddReloadListenersContextForgeImpl(evt::registerReloadListener));
        });
        eventBus.addListener((final RegisterClientReloadListenersEvent evt) -> {
            registerBuiltInItemModelRenderersReloadListeners(evt::registerReloadListener, modId, dynamicRenderers, availableFlags);
        });
        eventBus.addListener((final EntityRenderersEvent.AddLayers evt) -> {
            constructor.onRegisterLivingEntityRenderLayers(new LivingEntityRenderLayersContextForgeImpl(evt));
        });
        eventBus.addListener((final ColorHandlerEvent.Block evt) -> {
            constructor.onRegisterBlockColorProviders(new BlockColorProvidersContextForgeImpl(evt.getBlockColors()::register, evt.getBlockColors()));
        });
        eventBus.addListener((final ColorHandlerEvent.Item evt) -> {
            constructor.onRegisterItemColorProviders(new ItemColorProvidersContextForgeImpl(evt.getItemColors()::register, evt.getItemColors()));
        });
        eventBus.addListener((final AddPackFindersEvent evt) -> {
            if (evt.getPackType() == PackType.CLIENT_RESOURCES) {
                constructor.onAddResourcePackFinders(new ResourcePackSourcesContextForgeImpl(evt::addRepositorySource));
            }
        });
        eventBus.addListener((final RegisterShadersEvent evt) -> {
            constructor.onRegisterCoreShaders(new CoreShadersContextForgeImpl(evt::registerShader, evt.getResourceManager()));
        });
    }

    private static void onBakingCompleted(Consumer<DynamicBakingCompletedContext> consumer, String modId, ModelManager modelManager, Map<ResourceLocation, BakedModel> models, ModelBakery modelBakery) {
        try {
            consumer.accept(new DynamicBakingCompletedContextForgeImpl(modelManager, models, modelBakery));
        } catch (Exception e) {
            PuzzlesLib.LOGGER.error("Unable to execute additional resource pack model processing during baking completed phase provided by {}", modId, e);
        }
    }

    private static void onModifyBakingResult(Consumer<DynamicModifyBakingResultContext> consumer, String modId, Map<ResourceLocation, BakedModel> models, ModelBakery modelBakery) {
        try {
            consumer.accept(new DynamicModifyBakingResultContextImpl(models, modelBakery));
        } catch (Exception e) {
            PuzzlesLib.LOGGER.error("Unable to execute additional resource pack model processing during modify baking result phase provided by {}", modId, e);
        }
    }

    private static void registerBuiltInItemModelRenderersReloadListeners(Consumer<PreparableReloadListener> consumer, String modId, List<ResourceManagerReloadListener> dynamicRenderers, Set<ContentRegistrationFlags> availableFlags) {
        if (availableFlags.contains(ContentRegistrationFlags.DYNAMIC_RENDERERS)) {
            // always register this, the event runs before built-in model item renderers, so the list is always empty at this point
            consumer.accept((ResourceManagerReloadListener) (ResourceManager resourceManager) -> {
                for (ResourceManagerReloadListener listener : dynamicRenderers) {
                    try {
                        listener.onResourceManagerReload(resourceManager);
                    } catch (Exception e) {
                        PuzzlesLib.LOGGER.error("Unable to execute dynamic built-in model item renderers reload provided by {}", modId, e);
                    }
                }
            });
        }
    }
}
