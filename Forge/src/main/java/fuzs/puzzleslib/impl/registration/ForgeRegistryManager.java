package fuzs.puzzleslib.impl.registration;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import fuzs.puzzleslib.api.registration.v2.RegistryManager;
import fuzs.puzzleslib.api.registration.v2.RegistryReference;
import fuzs.puzzleslib.api.core.v1.ModLoader;
import fuzs.puzzleslib.api.registration.v2.builder.ExtendedMenuSupplier;
import fuzs.puzzleslib.api.registration.v2.builder.PoiTypeBuilder;
import fuzs.puzzleslib.api.core.v1.ModContainerHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * handles registering to forge registries
 * <p>this is a mod specific instance now for Fabric compatibility, Forge would support retrieving current namespace from mod loading context
 * <p>originally heavily inspired by RegistryHelper found in Vazkii's AutoRegLib mod
 */
public class ForgeRegistryManager implements RegistryManager {
    /**
     * registry data is stored for each mod separately so when registry events are fired every mod is responsible for registering their own stuff
     * this is important so that entries are registered for the proper namespace
     */
    private static final Map<String, ForgeRegistryManager> MOD_TO_REGISTRY = Maps.newConcurrentMap();

    /**
     * namespace for this instance
     */
    private final String namespace;
    /**
     * the mod event bus required for registering {@link DeferredRegister}
     */
    private final IEventBus modEventBus;
    /**
     * storage for {@link DeferredRegister} required for registering data on Forge
     */
    private final Map<ResourceKey<? extends Registry<?>>, DeferredRegister<?>> deferredRegisters = Maps.newIdentityHashMap();
    /**
     * mod loader sto register the next entry to, null by default for registering to any
     */
    @Nullable
    private Set<ModLoader> allowedModLoaders;

    /**
     * private constructor
     *
     * @param modId     namespace for this instance
     */
    private ForgeRegistryManager(String modId) {
        this.namespace = modId;
        this.modEventBus = ModContainerHelper.findModEventBus(modId);
    }

    @Override
    public String namespace() {
        return this.namespace;
    }

    @Override
    public RegistryManager whenOn(ModLoader... allowedModLoaders) {
        if (allowedModLoaders.length == 0) throw new IllegalArgumentException("Must provide at least one mod loader to register on");
        this.allowedModLoaders = ImmutableSet.copyOf(allowedModLoaders);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> RegistryReference<T> register(ResourceKey<? extends Registry<? super T>> registryKey, String path, Supplier<T> supplier) {
        Set<ModLoader> modLoaders = this.allowedModLoaders;
        this.allowedModLoaders = null;
        if (modLoaders != null && !modLoaders.contains(ModLoader.FORGE)) {
            return this.placeholder(registryKey, path);
        }
        DeferredRegister<?> register = this.deferredRegisters.computeIfAbsent(registryKey, key -> {
            DeferredRegister<T> deferredRegister = DeferredRegister.create((ResourceKey<? extends Registry<T>>) registryKey, this.namespace);
            deferredRegister.register(this.modEventBus);
            return deferredRegister;
        });
        if (StringUtils.isEmpty(path)) throw new IllegalArgumentException("Can't register object without name");
        RegistryObject<T> registryObject = ((DeferredRegister<T>) register).register(path, supplier);
        return new ForgeRegistryReference<>(registryObject, (ResourceKey<? extends Registry<T>>) registryKey);
    }

    @Override
    public RegistryReference<Item> registerSpawnEggItem(RegistryReference<EntityType<? extends Mob>> entityTypeReference, int backgroundColor, int highlightColor, Item.Properties itemProperties) {
        return this.registerItem(entityTypeReference.getResourceLocation().getPath() + "_spawn_egg", () -> new ForgeSpawnEggItem(entityTypeReference::get, backgroundColor, highlightColor, itemProperties));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends AbstractContainerMenu> RegistryReference<MenuType<T>> registerExtendedMenuType(String path, Supplier<ExtendedMenuSupplier<T>> entry) {
        return this.register((ResourceKey<Registry<MenuType<T>>>) (ResourceKey<?>) Registries.MENU, path, () -> new MenuType<>((IContainerFactory<T>) (containerId, inventory, data) -> entry.get().create(containerId, inventory, data)));
    }

    @Override
    public RegistryReference<PoiType> registerPoiTypeBuilder(String path, Supplier<PoiTypeBuilder> entry) {
        return this.register(Registries.POINT_OF_INTEREST_TYPE, path, () -> {
            PoiTypeBuilder builder = entry.get();
            return new PoiType(ImmutableSet.copyOf(builder.blocks()), builder.ticketCount(), builder.searchDistance());
        });
    }

    /**
     * creates a new registry manager for <code>modId</code> or returns an existing one
     *
     * @param modId     namespace used for registration
     * @return          new mod specific registry manager
     */
    public synchronized static RegistryManager of(String modId) {
        return MOD_TO_REGISTRY.computeIfAbsent(modId, ForgeRegistryManager::new);
    }
}