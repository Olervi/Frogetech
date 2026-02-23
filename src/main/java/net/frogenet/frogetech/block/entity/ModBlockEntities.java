package net.frogenet.frogetech.block.entity;

import net.frogenet.frogetech.Frogetech;
import net.frogenet.frogetech.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Frogetech.MODID);

    public static final Supplier<BlockEntityType<PedestalBlockEntity>> PEDESTAL_BE =
            BLOCK_ENTITIES.register("pedestal_be", () -> BlockEntityType.Builder.of(
                    PedestalBlockEntity::new, ModBlocks.PEDESTAL.get()).build(null));

    public static final Supplier<BlockEntityType<ToadToasterBlockEntity>> TOAD_TOASTER_BE =
            BLOCK_ENTITIES.register("toad_toaster_be", () -> BlockEntityType.Builder.of(
                    ToadToasterBlockEntity::new, ModBlocks.TOAD_TOASTER.get()).build(null));

    public static final Supplier<BlockEntityType<CableBlockEntity>> CABLE_BE =
            BLOCK_ENTITIES.register("cable_be", () -> BlockEntityType.Builder.of(
                    CableBlockEntity::new, ModBlocks.CABLE.get()).build(null));


    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
