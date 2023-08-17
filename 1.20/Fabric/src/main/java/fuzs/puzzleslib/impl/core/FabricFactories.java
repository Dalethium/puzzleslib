package fuzs.puzzleslib.impl.core;

import fuzs.puzzleslib.api.core.v1.ContentRegistrationFlags;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.Proxy;
import fuzs.puzzleslib.api.init.v2.GameRulesFactory;
import fuzs.puzzleslib.api.init.v2.PotionBrewingRegistry;
import fuzs.puzzleslib.api.item.v2.ToolTypeHelper;
import fuzs.puzzleslib.impl.event.FabricEventInvokerRegistryImpl;
import fuzs.puzzleslib.impl.init.FabricGameRulesFactory;
import fuzs.puzzleslib.impl.init.PotionBrewingRegistryFabric;
import fuzs.puzzleslib.impl.item.FabricToolTypeHelper;

import java.util.Set;

public final class FabricFactories implements CommonFactories {

    @Override
    public void constructMod(String modId, ModConstructor modConstructor, Set<ContentRegistrationFlags> availableFlags, Set<ContentRegistrationFlags> flagsToHandle) {
        FabricModConstructor.construct(modConstructor, modId, availableFlags, flagsToHandle);
    }

    @Override
    public ModContext getModContext(String modId) {
        return new FabricModContext(modId);
    }

    @Override
    public Proxy getClientProxy() {
        return new FabricClientProxy();
    }

    @Override
    public Proxy getServerProxy() {
        return new FabricServerProxy();
    }

    @Override
    public PotionBrewingRegistry getPotionBrewingRegistry() {
        return new PotionBrewingRegistryFabric();
    }

    @Override
    public GameRulesFactory getGameRulesFactory() {
        return new FabricGameRulesFactory();
    }

    @Override
    public void registerEventInvokers() {
        FabricEventInvokerRegistryImpl.register();
    }

    @Override
    public ToolTypeHelper getToolTypeHelper() {
        return new FabricToolTypeHelper();
    }
}
