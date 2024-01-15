package fuzs.puzzleslib.forge.impl.client.core;

import com.google.common.collect.Lists;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.particle.v1.ClientParticleTypes;
import fuzs.puzzleslib.api.core.v1.ContentRegistrationFlags;
import fuzs.puzzleslib.api.core.v1.resources.ForwardingReloadListenerHelper;
import fuzs.puzzleslib.forge.api.core.v1.ForgeModContainerHelper;
import fuzs.puzzleslib.forge.impl.client.core.context.*;
import fuzs.puzzleslib.forge.impl.core.context.AddReloadListenersContextForgeImpl;
import fuzs.puzzleslib.impl.client.core.context.BlockRenderTypesContextImpl;
import fuzs.puzzleslib.impl.client.core.context.FluidRenderTypesContextImpl;
import fuzs.puzzleslib.impl.client.particle.ClientParticleTypesImpl;
import fuzs.puzzleslib.impl.client.particle.ClientParticleTypesManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.List;
import java.util.Set;

public final class ForgeClientModConstructor {

    private ForgeClientModConstructor() {

    }


    public static void construct(ClientModConstructor constructor, String modId, Set<ContentRegistrationFlags> availableFlags, Set<ContentRegistrationFlags> flagsToHandle) {
        ForgeModContainerHelper.getOptionalModEventBus(modId).ifPresent(eventBus -> {
            registerModHandlers(constructor, modId, eventBus, availableFlags, flagsToHandle);
            constructor.onConstructMod();
        });
    }

    private static void registerModHandlers(ClientModConstructor constructor, String modId, IEventBus eventBus, Set<ContentRegistrationFlags> availableFlags, Set<ContentRegistrationFlags> flagsToHandle) {
        List<ResourceManagerReloadListener> dynamicRenderers = Lists.newArrayList();
        eventBus.addListener((final FMLClientSetupEvent evt) -> {
            // need to run this deferred as most registries here do not use concurrent maps
            evt.enqueueWork(() -> {
                constructor.onClientSetup();
                constructor.onRegisterItemModelProperties(new ItemModelPropertiesContextForgeImpl());
                constructor.onRegisterBuiltinModelItemRenderers(new BuiltinModelItemRendererContextForgeImpl(modId, dynamicRenderers));
                constructor.onRegisterBlockRenderTypes(new BlockRenderTypesContextImpl());
                constructor.onRegisterFluidRenderTypes(new FluidRenderTypesContextImpl());
            });
        });
        eventBus.addListener((final EntityRenderersEvent.RegisterRenderers evt) -> {
            constructor.onRegisterEntityRenderers(new EntityRenderersContextForgeImpl(evt::registerEntityRenderer));
            constructor.onRegisterBlockEntityRenderers(new BlockEntityRenderersContextForgeImpl(evt::registerBlockEntityRenderer));
        });
        eventBus.addListener((final RegisterClientTooltipComponentFactoriesEvent evt) -> {
            constructor.onRegisterClientTooltipComponents(new ClientTooltipComponentsContextForgeImpl(evt::register));
        });
        eventBus.addListener((final RegisterParticleProvidersEvent evt) -> {
            constructor.onRegisterParticleProviders(new ParticleProvidersContextForgeImpl(evt));
        });
        eventBus.addListener((final EntityRenderersEvent.RegisterLayerDefinitions evt) -> {
            constructor.onRegisterLayerDefinitions(new LayerDefinitionsContextForgeImpl(evt::registerLayerDefinition));
        });
        eventBus.addListener((final ModelEvent.RegisterAdditional evt) -> {
            constructor.onRegisterAdditionalModels(new AdditionalModelsContextForgeImpl(evt::register));
        });
        eventBus.addListener((final RegisterItemDecorationsEvent evt) -> {
            constructor.onRegisterItemDecorations(new ItemDecorationContextForgeImpl(evt::register));
        });
        eventBus.addListener((final RegisterEntitySpectatorShadersEvent evt) -> {
            constructor.onRegisterEntitySpectatorShaders(new EntitySpectatorShaderContextForgeImpl(evt::register));
        });
        eventBus.addListener((final EntityRenderersEvent.CreateSkullModels evt) -> {
            constructor.onRegisterSkullRenderers(new SkullRenderersContextForgeImpl(evt.getEntityModelSet(), evt::registerSkullModel));
        });
        eventBus.addListener((final RegisterClientReloadListenersEvent evt) -> {
            constructor.onRegisterResourcePackReloadListeners(new AddReloadListenersContextForgeImpl(modId, evt::registerReloadListener));
            if (availableFlags.contains(ContentRegistrationFlags.DYNAMIC_RENDERERS)) {
                evt.registerReloadListener(ForwardingReloadListenerHelper.fromResourceManagerReloadListeners(new ResourceLocation(modId, "built_in_model_item_renderers"), dynamicRenderers));
            }
            if (flagsToHandle.contains(ContentRegistrationFlags.CLIENT_PARTICLE_TYPES)) {
                ClientParticleTypesManager particleTypesManager = ((ClientParticleTypesImpl) ClientParticleTypes.INSTANCE).getParticleTypesManager(modId);
                evt.registerReloadListener(ForwardingReloadListenerHelper.fromReloadListener(new ResourceLocation(modId, "client_particle_types"), particleTypesManager));
            }
        });
        eventBus.addListener((final EntityRenderersEvent.AddLayers evt) -> {
            constructor.onRegisterLivingEntityRenderLayers(new LivingEntityRenderLayersContextForgeImpl(evt));
        });
        eventBus.addListener((final RegisterKeyMappingsEvent evt) -> {
            constructor.onRegisterKeyMappings(new KeyMappingsContextForgeImpl(evt::register));
        });
        eventBus.addListener((final RegisterColorHandlersEvent.Block evt) -> {
            constructor.onRegisterBlockColorProviders(new BlockColorProvidersContextForgeImpl(evt::register, evt.getBlockColors()));
        });
        eventBus.addListener((final RegisterColorHandlersEvent.Item evt) -> {
            constructor.onRegisterItemColorProviders(new ItemColorProvidersContextForgeImpl(evt::register, evt.getItemColors()));
        });
        eventBus.addListener((final AddPackFindersEvent evt) -> {
            if (evt.getPackType() == PackType.CLIENT_RESOURCES) {
                constructor.onAddResourcePackFinders(new ResourcePackSourcesContextForgeImpl(evt::addRepositorySource));
            }
        });
        eventBus.addListener((final RegisterShadersEvent evt) -> {
            constructor.onRegisterCoreShaders(new CoreShadersContextForgeImpl(evt::registerShader, evt.getResourceProvider()));
        });
    }
}
