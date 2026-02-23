package net.frogenet.frogetech.entity;

import net.frogenet.frogetech.Frogetech;
import net.frogenet.frogetech.entity.custom.FrogEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Frogetech.MODID);

    public static final Supplier<EntityType<FrogEntity>> FROG =
            ENTITY_TYPES.register("frog", () -> EntityType.Builder.of(FrogEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 0.6f)
                    .build("frog"));

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }


}
