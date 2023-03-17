package fuzs.puzzleslib.impl.core;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import fuzs.puzzleslib.api.biome.v1.BiomeLoadingPhase;
import fuzs.puzzleslib.api.core.v1.ContentRegistrationFlags;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.contexts.LootTablesContext;
import fuzs.puzzleslib.api.core.v1.contexts.RegisterCommandsContext;
import fuzs.puzzleslib.api.core.v1.contexts.SpawnPlacementsContext;
import fuzs.puzzleslib.impl.PuzzlesLib;
import fuzs.puzzleslib.impl.biome.BiomeLoadingHandler;
import fuzs.puzzleslib.impl.creativemodetab.CreativeModeTabConfiguratorImpl;
import fuzs.puzzleslib.mixin.accessor.FireBlockForgeAccessor;
import fuzs.puzzleslib.api.core.v1.ModContainerHelper;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * wrapper class for {@link ModConstructor} for calling all required registration methods at the correct time most things need events for registering
 * <p>we use this wrapper style to allow for already registered to be used within the registration methods instead of having to use suppliers
 */
public class ForgeModConstructor {
    private final ModConstructor constructor;
    private final Object2IntOpenHashMap<Item> fuelBurnTimes = new Object2IntOpenHashMap<>();
    private final Multimap<BiomeLoadingPhase, BiomeLoadingHandler.BiomeModificationData> biomeLoadingEntries = HashMultimap.create();

    private ForgeModConstructor(ModConstructor constructor) {
        this.constructor = constructor;
        constructor.onConstructMod();
    }

    @SubscribeEvent
    public void onCommonSetup(final FMLCommonSetupEvent evt) {
        this.constructor.onCommonSetup(evt::enqueueWork);
        this.constructor.onRegisterFuelBurnTimes((burnTime, item, items) -> {
            if (Mth.clamp(burnTime, 1, 32767) != burnTime) throw new IllegalArgumentException("fuel burn time is out of bounds");
            Objects.requireNonNull(item, "item is null");
            this.fuelBurnTimes.put(item.asItem(), burnTime);
            Objects.requireNonNull(items, "items is null");
            for (ItemLike other : items) {
                Objects.requireNonNull(other, "item is null");
                this.fuelBurnTimes.put(other.asItem(), burnTime);
            }
        });
        this.constructor.onRegisterBiomeModifications((phase, selector, modifier) -> {
            this.biomeLoadingEntries.put(phase, new BiomeLoadingHandler.BiomeModificationData(phase, selector, modifier));
        });
        this.constructor.onRegisterFlammableBlocks((encouragement, flammability, blocks) -> {
            Objects.requireNonNull(blocks, "blocks is null");
            for (Block block : blocks) {
                Objects.requireNonNull(block, "block is null");
                ((FireBlockForgeAccessor) Blocks.FIRE).puzzleslib$setFlammable(block, encouragement, flammability);
            }
        });
    }

    @SubscribeEvent
    public void onRegisterSpawnPlacement(final SpawnPlacementRegisterEvent evt) {
        this.constructor.onRegisterSpawnPlacements(new SpawnPlacementsContext() {

            @Override
            public <T extends Mob> void registerSpawnPlacement(EntityType<T> entityType, SpawnPlacements.Type location, Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> spawnPredicate) {
                evt.register(entityType, location, heightmap, spawnPredicate, SpawnPlacementRegisterEvent.Operation.REPLACE);
            }
        });
    }

    @SubscribeEvent
    public void onEntityAttributeCreation(final EntityAttributeCreationEvent evt) {
        this.constructor.onEntityAttributeCreation((EntityType<? extends LivingEntity> entity, AttributeSupplier.Builder builder) -> evt.put(entity, builder.build()));
    }

    @SubscribeEvent
    public void onEntityAttributeModification(final EntityAttributeModificationEvent evt) {
        this.constructor.onEntityAttributeModification(evt::add);
    }

    @SubscribeEvent
    public void onCreativeModeTab$Register(final CreativeModeTabEvent.Register evt) {
        this.constructor.onRegisterCreativeModeTabs(configurator -> {
            evt.registerCreativeModeTab(((CreativeModeTabConfiguratorImpl) configurator).getIdentifier(), this.configureCreativeModeTabBuilder((CreativeModeTabConfiguratorImpl) configurator));
        });
    }

    private Consumer<CreativeModeTab.Builder> configureCreativeModeTabBuilder(CreativeModeTabConfiguratorImpl configurator) {
        return builder -> {
            configurator.configure(builder);
            builder.title(Component.translatable("itemGroup.%s.%s".formatted(configurator.getIdentifier().getNamespace(), configurator.getIdentifier().getPath())));
            if (configurator.isHasSearchBar()) builder.withSearchBar();
            if (configurator.getIcons() != null) {
                builder.withTabFactory(other -> new CreativeModeTab(other) {
                    @Nullable
                    private ItemStack[] itemStacks;

                    @Override
                    public ItemStack getIconItem() {
                        // stolen from XFactHD, thanks :)
                        if (this.itemStacks == null) this.itemStacks = configurator.getIcons().get();
                        int index = (int) (System.currentTimeMillis() / 2000) % this.itemStacks.length;
                        return this.itemStacks[index];
                    }
                });
            }
        };
    }

    private LootTablesContext.Replace getLootTablesReplaceContext(LootTables lootManager, ResourceLocation id, LootTable lootTable, Consumer<LootTable> lootTableSetter) {
        return new LootTablesContext.Replace(lootManager, id, lootTable) {

            @Override
            public void setLootTable(LootTable table) {
                lootTableSetter.accept(table);
            }
        };
    }

    private LootTablesContext.Modify getLootTablesModifyContext(LootTables lootManager, ResourceLocation id, LootTable lootTable) {
        return new LootTablesContext.Modify(lootManager, id) {

            @Override
            public void addLootPool(LootPool pool) {
                lootTable.addPool(pool);
            }

            @SuppressWarnings("ConstantConditions")
            @Override
            public boolean removeLootPool(int index) {
                if (index == 0 && lootTable.removePool("main") != null) {
                    return true;
                }
                return lootTable.removePool("pool" + index) != null;
            }
        };
    }

    /**
     * construct the mod, calling all necessary registration methods
     * we don't need the object, it's only important for being registered to the necessary events buses
     *
     * @param modId the mod id for registering events on Forge to the correct mod event bus
     * @param constructor mod base class
     */
    public static void construct(ModConstructor constructor, String modId, ContentRegistrationFlags... contentRegistrations) {
        if (Strings.isBlank(modId)) throw new IllegalArgumentException("mod id cannot be empty");
        PuzzlesLib.LOGGER.info("Constructing common components for mod {}", modId);
        ForgeModConstructor forgeModConstructor = new ForgeModConstructor(constructor);
        IEventBus modEventBus = ModContainerHelper.findModEventBus(modId);
        modEventBus.register(forgeModConstructor);
        // we need to manually register events for the normal event bus
        // as you cannot have both event bus types going through SubscribeEvent annotated methods in the same class
        MinecraftForge.EVENT_BUS.addListener((final FurnaceFuelBurnTimeEvent evt) -> {
            Item item = evt.getItemStack().getItem();
            if (forgeModConstructor.fuelBurnTimes.containsKey(item)) {
                evt.setBurnTime(forgeModConstructor.fuelBurnTimes.getInt(item));
            }
        });
        MinecraftForge.EVENT_BUS.addListener((final RegisterCommandsEvent evt) -> {
           constructor.onRegisterCommands(new RegisterCommandsContext(evt.getDispatcher(), evt.getBuildContext(), evt.getCommandSelection()));
        });
        MinecraftForge.EVENT_BUS.addListener((final LootTableLoadEvent evt) -> {
            constructor.onLootTableReplacement(forgeModConstructor.getLootTablesReplaceContext(evt.getLootTableManager(), evt.getName(), evt.getTable(), evt::setTable));
            constructor.onLootTableModification(forgeModConstructor.getLootTablesModifyContext(evt.getLootTableManager(), evt.getName(), evt.getTable()));
        });
        if (ArrayUtils.contains(contentRegistrations, ContentRegistrationFlags.BIOMES)) {
            BiomeLoadingHandler.register(modId, modEventBus, forgeModConstructor.biomeLoadingEntries);
        }
    }
}