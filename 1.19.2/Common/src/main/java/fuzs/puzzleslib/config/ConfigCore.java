package fuzs.puzzleslib.config;

import fuzs.puzzleslib.config.core.AbstractConfigBuilder;

/**
 * config template interface for each config category, can be nested (fields in a subclass can be once again of type {@link ConfigCore})
 */
public interface ConfigCore {

    /**
     * add config entries
     *
     * @param builder builder to add entries to
     * @param callback register save callback
     */
    default void addToBuilder(AbstractConfigBuilder builder, ValueCallback callback) {

    }

    /**
     * transform config options to proper type after reload, e.g. strings to registry entries
     */
    default void afterConfigReload() {

    }
}