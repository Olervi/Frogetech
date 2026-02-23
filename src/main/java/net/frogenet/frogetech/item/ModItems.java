package net.frogenet.frogetech.item;

import net.frogenet.frogetech.Frogetech;
import net.frogenet.frogetech.entity.ModEntities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Frogetech.MODID);

    public static final DeferredItem<Item> GUNK_BALL = ITEMS.registerItem("gunk_ball",
            Item::new, new Item.Properties());

    public static final DeferredItem<Item> BASE_FROG_SPAWN_EGG = ITEMS.registerItem("base_frog_spawn_egg",
            properties -> new SpawnEggItem(ModEntities.FROG.get(), 0x006309, 0x4ad240, properties));


    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
