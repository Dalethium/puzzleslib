package fuzs.puzzleslib.impl.client.event;

import com.mojang.blaze3d.platform.Window;
import fuzs.puzzleslib.api.client.event.v1.*;
import fuzs.puzzleslib.api.event.v1.LoadCompleteCallback;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static fuzs.puzzleslib.impl.event.FabricEventInvokerRegistryImpl.INSTANCE;

@SuppressWarnings("unchecked")
public final class FabricClientEventInvokers {

    public static void register() {
        INSTANCE.register(LoadCompleteCallback.class, ClientLifecycleEvents.CLIENT_STARTED, callback -> {
            return (Minecraft client) -> {
                callback.onLoadComplete();
            };
        });
        INSTANCE.register(ClientTickEvents.Start.class, net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.START_CLIENT_TICK, callback -> {
            return callback::onStartClientTick;
        });
        INSTANCE.register(ClientTickEvents.End.class, net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK, callback -> {
            return callback::onEndClientTick;
        });
        INSTANCE.register(RenderGuiCallback.class, HudRenderCallback.EVENT, callback -> {
            return (matrixStack, tickDelta) -> {
                Minecraft minecraft = Minecraft.getInstance();
                Window window = minecraft.getWindow();
                callback.onRenderGui(minecraft, matrixStack, tickDelta, window.getGuiScaledWidth(), window.getGuiScaledHeight());
            };
        });
        INSTANCE.register(ItemTooltipCallback.class, net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT, callback -> {
            return (stack, context, lines) -> callback.onItemTooltip(stack, Minecraft.getInstance().player, lines, context);
        });
        INSTANCE.register(RenderNameTagCallback.class, FabricClientEvents.RENDER_NAME_TAG);
        INSTANCE.register(ContainerScreenEvents.Background.class, FabricScreenEvents.CONTAINER_SCREEN_BACKGROUND);
        INSTANCE.register(ContainerScreenEvents.Foreground.class, FabricScreenEvents.CONTAINER_SCREEN_FOREGROUND);
        INSTANCE.register(InventoryMobEffectsCallback.class, FabricScreenEvents.INVENTORY_MOB_EFFECTS);
        INSTANCE.register(ScreenOpeningCallback.class, FabricScreenEvents.SCREEN_OPENING);
        INSTANCE.register(ComputeFovModifierCallback.class, FabricClientEvents.COMPUTE_FOV_MODIFIER);
        INSTANCE.register(ScreenEvents.BeforeInit.class, net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.BEFORE_INIT, callback -> {
            return (client, screen, scaledWidth, scaledHeight) -> {
                callback.onBeforeInit(client, screen, scaledWidth, scaledHeight, Collections.unmodifiableList(Screens.getButtons(screen)));
            };
        });
        INSTANCE.register(ScreenEvents.AfterInit.class, net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AFTER_INIT, callback -> {
            return (client, screen, scaledWidth, scaledHeight) -> {
                List<AbstractWidget> widgets = Screens.getButtons(screen);
                callback.onAfterInit(client, screen, scaledWidth, scaledHeight, Collections.unmodifiableList(widgets), widgets::add, widgets::remove);
            };
        });
        registerScreenEvent(ScreenEvents.Remove.class, net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.Remove.class, callback -> {
            return callback::onRemove;
        }, net.fabricmc.fabric.api.client.screen.v1.ScreenEvents::remove);
        registerScreenEvent(ScreenEvents.BeforeRender.class, net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.BeforeRender.class, callback -> {
            return callback::onBeforeRender;
        }, net.fabricmc.fabric.api.client.screen.v1.ScreenEvents::beforeRender);
        registerScreenEvent(ScreenEvents.AfterRender.class, net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AfterRender.class, callback -> {
            return callback::onAfterRender;
        }, net.fabricmc.fabric.api.client.screen.v1.ScreenEvents::afterRender);
        registerScreenEvent(ScreenMouseEvents.BeforeMouseClick.class, net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents.AllowMouseClick.class, callback -> {
            return (screen, mouseX, mouseY, button) -> {
                return callback.onBeforeMouseClick(screen, mouseX, mouseY, button).isPass();
            };
        }, net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents::allowMouseClick);
        registerScreenEvent(ScreenMouseEvents.AfterMouseClick.class, net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents.AfterMouseClick.class, callback -> {
            return callback::onAfterMouseClick;
        }, net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents::afterMouseClick);
        registerScreenEvent(ScreenMouseEvents.BeforeMouseRelease.class, net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents.AllowMouseRelease.class, callback -> {
            return (screen, mouseX, mouseY, button) -> {
                return callback.onBeforeMouseRelease(screen, mouseX, mouseY, button).isPass();
            };
        }, net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents::allowMouseRelease);
        registerScreenEvent(ScreenMouseEvents.AfterMouseRelease.class, net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents.AfterMouseRelease.class, callback -> {
            return callback::onAfterMouseRelease;
        }, net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents::afterMouseRelease);
        registerScreenEvent(ScreenMouseEvents.BeforeMouseScroll.class, net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents.AllowMouseScroll.class, callback -> {
            return (screen, mouseX, mouseY, horizontalAmount, verticalAmount) -> {
                return callback.onBeforeMouseScroll(screen, mouseX, mouseY, horizontalAmount, verticalAmount).isPass();
            };
        }, net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents::allowMouseScroll);
        registerScreenEvent(ScreenMouseEvents.AfterMouseScroll.class, net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents.AfterMouseScroll.class, callback -> {
            return callback::onAfterMouseScroll;
        }, net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents::afterMouseScroll);
        registerScreenEvent(ScreenMouseEvents.BeforeMouseDrag.class, ExtraScreenMouseEvents.AllowMouseDrag.class, callback -> {
            return (screen, mouseX, mouseY, button, dragX, dragY) -> {
                return callback.onBeforeMouseDrag(screen, mouseX, mouseY, button, dragX, dragY).isPass();
            };
        }, ExtraScreenMouseEvents::allowMouseDrag);
        registerScreenEvent(ScreenMouseEvents.AfterMouseDrag.class, ExtraScreenMouseEvents.AfterMouseDrag.class, callback -> {
            return callback::onAfterMouseDrag;
        }, ExtraScreenMouseEvents::afterMouseDrag);
        registerScreenEvent(ScreenKeyboardEvents.BeforeKeyPress.class, net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents.AllowKeyPress.class, callback -> {
            return (Screen screen, int key, int scancode, int modifiers) -> {
                return callback.onBeforeKeyPress(screen, key, scancode, modifiers).isPass();
            };
        }, net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents::allowKeyPress);
        registerScreenEvent(ScreenKeyboardEvents.AfterKeyPress.class, net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents.AfterKeyPress.class, callback -> {
            return callback::onAfterKeyPress;
        }, net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents::afterKeyPress);
        registerScreenEvent(ScreenKeyboardEvents.BeforeKeyRelease.class, net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents.AllowKeyRelease.class, callback -> {
            return (Screen screen, int key, int scancode, int modifiers) -> {
                return callback.onBeforeKeyRelease(screen, key, scancode, modifiers).isPass();
            };
        }, net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents::allowKeyRelease);
        registerScreenEvent(ScreenKeyboardEvents.AfterKeyRelease.class, net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents.AfterKeyRelease.class, callback -> {
            return callback::onAfterKeyRelease;
        }, net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents::afterKeyRelease);
        INSTANCE.register(RenderGuiElementEvents.Before.class, (context, applyToInvoker, removeInvoker) -> {
            Objects.requireNonNull(context, "context is null");
            RenderGuiElementEvents.GuiOverlay overlay = (RenderGuiElementEvents.GuiOverlay) context;
            applyToInvoker.accept(FabricClientEvents.beforeRenderGuiElement(overlay.id()));
        });
        INSTANCE.register(RenderGuiElementEvents.After.class, (context, applyToInvoker, removeInvoker) -> {
            Objects.requireNonNull(context, "context is null");
            RenderGuiElementEvents.GuiOverlay overlay = (RenderGuiElementEvents.GuiOverlay) context;
            applyToInvoker.accept(FabricClientEvents.afterRenderGuiElement(overlay.id()));
        });
        INSTANCE.register(CustomizeChatPanelCallback.class, FabricClientEvents.CUSTOMIZE_CHAT_PANEL);
        INSTANCE.register(ClientEntityLevelEvents.Load.class, FabricClientEvents.ENTITY_LOAD);
        INSTANCE.register(ClientEntityLevelEvents.Unload.class, ClientEntityEvents.ENTITY_UNLOAD, callback -> {
            return callback::onEntityUnload;
        });
        INSTANCE.register(InputEvents.BeforeMouseAction.class, FabricClientEvents.BEFORE_MOUSE_ACTION);
        INSTANCE.register(InputEvents.AfterMouseAction.class, FabricClientEvents.AFTER_MOUSE_ACTION);
        INSTANCE.register(InputEvents.BeforeMouseScroll.class, FabricClientEvents.BEFORE_MOUSE_SCROLL);
        INSTANCE.register(InputEvents.AfterMouseScroll.class, FabricClientEvents.AFTER_MOUSE_SCROLL);
        INSTANCE.register(InputEvents.BeforeKeyAction.class, FabricClientEvents.BEFORE_KEY_ACTION);
        INSTANCE.register(InputEvents.AfterKeyAction.class, FabricClientEvents.AFTER_KEY_ACTION);
        INSTANCE.register(RenderPlayerEvents.Before.class, FabricClientEvents.BEFORE_RENDER_PLAYER);
        INSTANCE.register(RenderPlayerEvents.After.class, FabricClientEvents.AFTER_RENDER_PLAYER);
        INSTANCE.register(RenderHandCallback.class, FabricClientEvents.RENDER_HAND);
        INSTANCE.register(ComputeCameraAnglesCallback.class, FabricClientEvents.COMPUTE_CAMERA_ANGLES);
        INSTANCE.register(ClientLevelTickEvents.Start.class, net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.START_WORLD_TICK, callback -> {
            return (ClientLevel world) -> {
                callback.onStartLevelTick(Minecraft.getInstance(), world);
            };
        });
        INSTANCE.register(ClientLevelTickEvents.End.class, net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_WORLD_TICK, callback -> {
            return (ClientLevel world) -> {
                callback.onEndLevelTick(Minecraft.getInstance(), world);
            };
        });
        INSTANCE.register(ClientChunkEvents.Load.class, net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents.CHUNK_LOAD, callback -> {
            return callback::onChunkLoad;
        });
        INSTANCE.register(ClientChunkEvents.Unload.class, net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents.CHUNK_UNLOAD, callback -> {
            return callback::onChunkUnload;
        });
        INSTANCE.register(ClientPlayerEvents.LoggedIn.class, FabricClientEvents.PLAYER_LOGGED_IN);
        INSTANCE.register(ClientPlayerEvents.LoggedOut.class, FabricClientEvents.PLAYER_LOGGED_OUT);
        INSTANCE.register(ClientPlayerEvents.Copy.class, FabricClientEvents.PLAYER_COPY);
        INSTANCE.register(InteractionInputEvents.Attack.class, FabricClientEvents.ATTACK_INTERACTION_INPUT);
        INSTANCE.register(ClientLevelEvents.Load.class, FabricClientEvents.LOAD_LEVEL);
        INSTANCE.register(ClientLevelEvents.Unload.class, FabricClientEvents.UNLOAD_LEVEL);
        INSTANCE.register(MovementInputUpdateCallback.class, FabricClientEvents.MOVEMENT_INPUT_UPDATE);
        INSTANCE.register(ModelEvents.ModifyBakingResult.class, FabricClientEvents.MODIFY_BAKING_RESULT);
        INSTANCE.register(ModelEvents.BakingCompleted.class, FabricClientEvents.BAKING_COMPLETED);
        INSTANCE.register(RenderBlockOverlayCallback.class, FabricClientEvents.RENDER_BLOCK_OVERLAY);
        INSTANCE.register(FogEvents.Render.class, FabricClientEvents.RENDER_FOG);
        INSTANCE.register(FogEvents.ComputeColor.class, FabricClientEvents.COMPUTE_FOG_COLOR);
        INSTANCE.register(ScreenTooltipEvents.Render.class, FabricScreenEvents.RENDER_TOOLTIP);
        INSTANCE.register(RenderHighlightCallback.class, WorldRenderEvents.BEFORE_BLOCK_OUTLINE, callback -> {
            return (WorldRenderContext context, @Nullable HitResult hitResult) -> {
                if (hitResult == null || hitResult.getType() == HitResult.Type.MISS || hitResult.getType() == HitResult.Type.BLOCK && !context.blockOutlines()) return true;
                Minecraft minecraft = Minecraft.getInstance();
                if (!(minecraft.getCameraEntity() instanceof Player) || minecraft.options.hideGui) return true;
                EventResult result = callback.onRenderHighlight(context.worldRenderer(), context.camera(), context.gameRenderer(), hitResult, context.tickDelta(), context.matrixStack(), context.consumers(), context.world());
                return result.isPass();
            };
        });
        INSTANCE.register(RenderLevelEvents.AfterTerrain.class, WorldRenderEvents.BEFORE_ENTITIES, callback -> {
            return (WorldRenderContext context) -> {
                callback.onRenderLevelAfterTerrain(context.worldRenderer(), context.camera(), context.gameRenderer(), context.tickDelta(), context.matrixStack(), context.projectionMatrix(), context.frustum(), context.world());
            };
        });
        INSTANCE.register(RenderLevelEvents.AfterEntities.class, WorldRenderEvents.AFTER_ENTITIES, callback -> {
            return (WorldRenderContext context) -> {
                callback.onRenderLevelAfterEntities(context.worldRenderer(), context.camera(), context.gameRenderer(), context.tickDelta(), context.matrixStack(), context.projectionMatrix(), context.frustum(), context.world());
            };
        });
        INSTANCE.register(RenderLevelEvents.AfterTranslucent.class, WorldRenderEvents.AFTER_TRANSLUCENT, callback -> {
            return (WorldRenderContext context) -> {
                callback.onRenderLevelAfterTranslucent(context.worldRenderer(), context.camera(), context.gameRenderer(), context.tickDelta(), context.matrixStack(), context.projectionMatrix(), context.frustum(), context.world());
            };
        });
        INSTANCE.register(RenderLevelEvents.AfterLevel.class, WorldRenderEvents.END, callback -> {
            return (WorldRenderContext context) -> {
                callback.onRenderLevelAfterLevel(context.worldRenderer(), context.camera(), context.gameRenderer(), context.tickDelta(), context.matrixStack(), context.projectionMatrix(), context.frustum(), context.world());
            };
        });
        INSTANCE.register(GameRenderEvents.Before.class, FabricClientEvents.BEFORE_GAME_RENDER);
        INSTANCE.register(GameRenderEvents.After.class, FabricClientEvents.AFTER_GAME_RENDER);
    }

    private static <T, E> void registerScreenEvent(Class<T> clazz, Class<E> eventType, Function<T, E> converter, Function<Screen, Event<E>> eventGetter) {
        INSTANCE.register(clazz, eventType, converter, (context, applyToInvoker, removeInvoker) -> {
            // we need to keep our own event invokers during the whole pre-init phase to guarantee phase ordering is applied correctly,
            // since this is managed in the event invokers and there seems to be no way to handle it with just the Fabric event
            // (since the Fabric event doesn't allow for retrieving already applied event phase orders),
            // so we register all screen events during pre-init, which allows post-init to already clear our internal map again
            net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
                if (((Class<?>) context).isInstance(screen)) applyToInvoker.accept(eventGetter.apply(screen));
            });
            net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
                if (((Class<?>) context).isInstance(screen)) removeInvoker.accept(eventGetter.apply(screen));
            });
        });
    }
}
