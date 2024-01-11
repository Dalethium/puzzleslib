package fuzs.puzzleslib.impl.networking;

import com.google.common.collect.Lists;
import fuzs.puzzleslib.api.networking.v3.ClientboundMessage;
import fuzs.puzzleslib.api.networking.v3.NetworkHandlerV3;
import fuzs.puzzleslib.api.networking.v3.ServerboundMessage;

import java.util.List;

public interface NetworkHandlerRegistry extends NetworkHandlerV3 {

    /**
     * register a message that will be sent to clients
     *
     * @param clazz message class type
     * @param <T>   message implementation
     */
    <T extends Record & ClientboundMessage<T>> void registerClientbound(Class<?> clazz);

    /**
     * register a message that will be sent to servers
     *
     * @param clazz message class type
     * @param <T>   message implementation
     */
    <T extends Record & ServerboundMessage<T>> void registerServerbound(Class<?> clazz);

    abstract class BuilderImpl implements Builder {
        protected final String modId;
        private final List<Class<?>> clientboundMessages = Lists.newArrayList();
        private final List<Class<?>> serverboundMessages = Lists.newArrayList();
        protected boolean clientAcceptsVanillaOrMissing;
        protected boolean serverAcceptsVanillaOrMissing;

        protected BuilderImpl(String modId) {
            this.modId = modId;
        }

        @Override
        public <T extends Record & ClientboundMessage<T>> Builder registerClientbound(Class<T> clazz) {
            if (this.clientboundMessages.contains(clazz)) throw new IllegalStateException("Duplicate message of type %s".formatted(clazz));
            this.clientboundMessages.add(clazz);
            return this;
        }

        @Override
        public <T extends Record & ServerboundMessage<T>> Builder registerServerbound(Class<T> clazz) {
            if (this.serverboundMessages.contains(clazz)) throw new IllegalStateException("Duplicate message of type %s".formatted(clazz));
            this.serverboundMessages.add(clazz);
            return this;
        }

        @Override
        public Builder clientAcceptsVanillaOrMissing() {
            this.clientAcceptsVanillaOrMissing = true;
            return this;
        }

        @Override
        public Builder serverAcceptsVanillaOrMissing() {
            this.serverAcceptsVanillaOrMissing = true;
            return this;
        }

        @Override
        public NetworkHandlerV3 build() {
            NetworkHandlerRegistry networkHandler = this.getHandler();
            this.registerAll(networkHandler);
            return networkHandler;
        }

        protected abstract NetworkHandlerRegistry getHandler();

        private void registerAll(NetworkHandlerRegistry networkHandler) {
            for (Class<?> message : this.clientboundMessages) {
                networkHandler.registerClientbound(message);
            }
            for (Class<?> message : this.serverboundMessages) {
                networkHandler.registerServerbound(message);
            }
        }
    }
}
