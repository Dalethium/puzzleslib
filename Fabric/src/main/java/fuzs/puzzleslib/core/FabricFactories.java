package fuzs.puzzleslib.core;

import fuzs.puzzleslib.config.AbstractConfig;
import fuzs.puzzleslib.config.ConfigHolder;
import fuzs.puzzleslib.config.FabricConfigHolderImpl;
import fuzs.puzzleslib.network.FabricNetworkHandler;
import fuzs.puzzleslib.network.NetworkHandler;
import fuzs.puzzleslib.proxy.FabricClientProxy;
import fuzs.puzzleslib.proxy.FabricServerProxy;
import fuzs.puzzleslib.proxy.Proxy;

import java.util.function.Supplier;

public class FabricFactories implements CommonFactories {

    @Override
    public NetworkHandler network(String modId, boolean clientAcceptsVanillaOrMissing, boolean serverAcceptsVanillaOrMissing) {
        return FabricNetworkHandler.of(modId);
    }

    @Override
    public Supplier<Proxy> clientProxy() {
        return () -> new FabricClientProxy();
    }

    @Override
    public Supplier<Proxy> serverProxy() {
        return () -> new FabricServerProxy();
    }

    @Override
    public <C extends AbstractConfig, S extends AbstractConfig> ConfigHolder<C, S> config(Supplier<C> client, Supplier<S> server) {
        return new FabricConfigHolderImpl<>(client, server);
    }

    @Override
    public <C extends AbstractConfig> ConfigHolder<C, AbstractConfig> clientConfig(Supplier<C> client) {
        return new FabricConfigHolderImpl<>(client, () -> null);
    }

    @Override
    public <S extends AbstractConfig> ConfigHolder<AbstractConfig, S> serverConfig(Supplier<S> server) {
        return new FabricConfigHolderImpl<>(() -> null, server);
    }
}