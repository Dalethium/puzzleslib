package fuzs.puzzleslib.impl.core;

import fuzs.puzzleslib.api.core.v1.ContentRegistrationFlags;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.Proxy;
import fuzs.puzzleslib.api.event.v1.core.EventInvoker;
import fuzs.puzzleslib.api.init.v2.GameRulesFactory;
import fuzs.puzzleslib.api.init.v2.PotionBrewingRegistry;
import fuzs.puzzleslib.api.item.v2.ToolTypeHelper;
import fuzs.puzzleslib.impl.event.ForgeEventInvokerRegistryImpl;
import fuzs.puzzleslib.impl.init.ForgeGameRulesFactory;
import fuzs.puzzleslib.impl.init.PotionBrewingRegistryForge;
import fuzs.puzzleslib.impl.item.ForgeToolTypeHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Supplier;

public final class ForgeFactories implements CommonFactories {

    @Override
    public void constructMod(String modId, ModConstructor modConstructor, Set<ContentRegistrationFlags> availableFlags, Set<ContentRegistrationFlags> flagsToHandle) {
        ForgeModConstructor.construct(modConstructor, modId, availableFlags, flagsToHandle);
    }

    @Override
    public ModContext getModContext(String modId) {
        return new ForgeModContext(modId);
    }

    @Override
    public Proxy getClientProxy() {
        return new ForgeClientProxy();
    }

    @Override
    public Proxy getServerProxy() {
        return new ForgeServerProxy();
    }

    @Override
    public PotionBrewingRegistry getPotionBrewingRegistry() {
        return new PotionBrewingRegistryForge();
    }

    @Override
    public GameRulesFactory getGameRulesFactory() {
        return new ForgeGameRulesFactory();
    }

    @Override
    public <T> EventInvoker<T> getEventInvoker(Class<T> clazz, @Nullable Object context) {
        return ForgeEventInvokerRegistryImpl.INSTANCE.lookup(clazz, context);
    }

    @Override
    public ToolTypeHelper getToolTypeHelper() {
        return new ForgeToolTypeHelper();
    }
}
