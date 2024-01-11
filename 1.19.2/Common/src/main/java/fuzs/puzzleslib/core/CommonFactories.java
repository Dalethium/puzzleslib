package fuzs.puzzleslib.core;

import fuzs.puzzleslib.api.networking.v3.NetworkHandlerV3;
import fuzs.puzzleslib.capability.CapabilityController;
import fuzs.puzzleslib.config.ConfigCore;
import fuzs.puzzleslib.config.ConfigHolder;
import fuzs.puzzleslib.init.PotionBrewingRegistry;
import fuzs.puzzleslib.init.RegistryManager;
import fuzs.puzzleslib.network.NetworkHandler;
import fuzs.puzzleslib.proxy.Proxy;
import fuzs.puzzleslib.util.PuzzlesUtil;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * all sorts of instance factories that need to be created on a per-mod basis
 */
public interface CommonFactories {
    /**
     * instance of the common factories SPI
     */
    CommonFactories INSTANCE = PuzzlesUtil.loadServiceProvider(CommonFactories.class);

    /**
     * this is very much unnecessary as the method is only ever called from loader specific code anyway which does have
     * access to the specific mod constructor, but for simplifying things and having this method in a common place we keep it here
     *
     * @param modId                the mod id for registering events on Forge to the correct mod event bus
     * @return provides a consumer for loading a mod being provided the base class
     */
    default Consumer<ModConstructor> modConstructor(String modId) {
        return this.modConstructor(modId, new ContentRegistrationFlags[0]);
    }

    /**
     * this is very much unnecessary as the method is only ever called from loader specific code anyway which does have
     * access to the specific mod constructor, but for simplifying things and having this method in a common place we keep it here
     *
     * @param modId                the mod id for registering events on Forge to the correct mod event bus
     * @param contentRegistrations specific content this mod uses that needs to be additionally registered
     * @return provides a consumer for loading a mod being provided the base class
     */
    Consumer<ModConstructor> modConstructor(String modId, ContentRegistrationFlags... contentRegistrations);

    /**
     * creates a new network handler
     *
     * @param modId id for channel name
     * @return mod specific network handler with default channel
     */
    default NetworkHandler network(String modId) {
        return this.network(modId, false, false);
    }

    /**
     * creates a new network handler
     *
     * @param modId                         id for channel name
     * @param clientAcceptsVanillaOrMissing are servers without this mod or vanilla compatible
     * @param serverAcceptsVanillaOrMissing are clients without this mod or vanilla compatible
     * @return mod specific network handler with configured channel
     */
    NetworkHandler network(String modId, boolean clientAcceptsVanillaOrMissing, boolean serverAcceptsVanillaOrMissing);

    /**
     * creates a new network handler
     *
     * @param modId id for channel name
     * @return mod specific network handler with default channel
     */
    @ApiStatus.Internal
    NetworkHandlerV3.Builder networkingV3(String modId);

    /**
     * internal factory for client proxy, use {@link Proxy#INSTANCE}
     *
     * @return provides the client proxy supplier
     */
    @ApiStatus.Internal
    Supplier<Proxy> clientProxy();

    /**
     * internal factory for server proxy, use {@link Proxy#INSTANCE}
     *
     * @return provides the server proxy supplier
     */
    @ApiStatus.Internal
    Supplier<Proxy> serverProxy();

    /**
     * register a new client config to the holder/builder
     * <p>just an overload for {@link ConfigHolder.Builder#clientConfig} that also creates a new builder instance
     *
     * @param clazz        client config main class
     * @param clientConfig client config factory
     * @param <T>          client config type
     * @return the builder we are working with
     */
    <T extends ConfigCore> ConfigHolder.Builder clientConfig(Class<T> clazz, Supplier<T> clientConfig);

    /**
     * register a new client config to the holder/builder
     * <p>just an overload for {@link ConfigHolder.Builder#commonConfig} that also creates a new builder instance
     *
     * @param clazz        common config main class
     * @param commonConfig common config factory
     * @param <T>          common config type
     * @return the builder we are working with
     */
    <T extends ConfigCore> ConfigHolder.Builder commonConfig(Class<T> clazz, Supplier<T> commonConfig);

    /**
     * register a new client config to the holder/builder
     * <p>just an overload for {@link ConfigHolder.Builder#serverConfig} that also creates a new builder instance
     *
     * @param clazz        server config main class
     * @param serverConfig server config factory
     * @param <T>          server config type
     * @return the builder we are working with
     */
    <T extends ConfigCore> ConfigHolder.Builder serverConfig(Class<T> clazz, Supplier<T> serverConfig);

    /**
     * creates a new registry manager for <code>namespace</code> or returns an existing one
     *
     * @param modId namespace used for registration
     * @return new mod specific registry manager
     */
    default RegistryManager registration(String modId) {
        return this.registration(modId, false);
    }

    /**
     * creates a new registry manager for <code>namespace</code> or returns an existing one
     *
     * @param modId    namespace used for registration
     * @param deferred defer registration for this manager until {@link RegistryManager#applyRegistration()} is called
     * @return new mod specific registry manager
     */
    RegistryManager registration(String modId, boolean deferred);

    /**
     * creates a new capability controller for <code>namespace</code> or returns an existing one
     *
     * @param modId namespace used for registration
     * @return the mod specific capability controller
     */
    CapabilityController capabilities(String modId);

    /**
     * Provides a mod loader specific {@link PotionBrewingRegistry} singleton instance.
     *
     * @return the instance
     */
    PotionBrewingRegistry getPotionBrewingRegistry();
}
