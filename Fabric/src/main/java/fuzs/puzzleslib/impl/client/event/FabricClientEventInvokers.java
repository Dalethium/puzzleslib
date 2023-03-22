package fuzs.puzzleslib.impl.client.event;

import com.mojang.blaze3d.platform.Window;
import fuzs.puzzleslib.api.client.event.v1.ClientTickEvents;
import fuzs.puzzleslib.api.client.event.v1.RenderGuiCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;

import static fuzs.puzzleslib.impl.event.FabricEventInvokerRegistryImpl.INSTANCE;

public final class FabricClientEventInvokers {

    public static void register() {
        INSTANCE.register(ClientTickEvents.Start.class, net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.START_CLIENT_TICK, callback -> {
            return callback::onStartTick;
        });
        INSTANCE.register(ClientTickEvents.End.class, net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK, callback -> {
            return callback::onEndTick;
        });
        INSTANCE.register(RenderGuiCallback.class, HudRenderCallback.EVENT, callback -> {
            return (matrixStack, tickDelta) -> {
                Minecraft minecraft = Minecraft.getInstance();
                Window window = minecraft.getWindow();
                callback.onRenderGui(minecraft, matrixStack, tickDelta, window.getGuiScaledWidth(), window.getGuiScaledHeight());
            };
        });
    }
}
