package fuzs.puzzleslib.proxy;

import fuzs.puzzleslib.network.Message;
import fuzs.puzzleslib.api.networking.v3.ClientboundMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.function.Function;

public class FabricClientProxy extends FabricServerProxy {

    @Override
    public Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public Level getClientLevel() {
        return Minecraft.getInstance().level;
    }

    @Override
    public Connection getClientConnection() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        Objects.requireNonNull(connection, "Cannot send packets when not in game!");
        return connection.getConnection();
    }

    @Override
    public Object getClientInstance() {
        return Minecraft.getInstance();
    }

    @Override
    public boolean hasControlDown() {
        return Screen.hasControlDown();
    }

    @Override
    public boolean hasShiftDown() {
        return Screen.hasShiftDown();
    }

    @Override
    public boolean hasAltDown() {
        return Screen.hasAltDown();
    }

    @Override
    public <T extends Message<T>> void registerClientReceiver(ResourceLocation channelName, Function<FriendlyByteBuf, T> factory) {
        ClientPlayNetworking.registerGlobalReceiver(channelName, (Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) -> {
            T message = factory.apply(buf);
            client.execute(() -> message.makeHandler().handle(message, client.player, client));
        });
    }

    @Override
    public <T extends Record & ClientboundMessage<T>> void registerClientReceiverV2(ResourceLocation channelName, Function<FriendlyByteBuf, T> factory) {
        ClientPlayNetworking.registerGlobalReceiver(channelName, (Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) -> {
            T message = factory.apply(buf);
            client.execute(() -> {
                LocalPlayer player = client.player;
                Objects.requireNonNull(player, "player is null");
                message.getHandler().handle(message, client, handler, player, client.level);
            });
        });
    }
}
