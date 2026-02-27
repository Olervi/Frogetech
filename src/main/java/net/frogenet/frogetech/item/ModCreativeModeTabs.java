package net.frogenet.frogetech.item;

import net.frogenet.frogetech.Frogetech;
import net.frogenet.frogetech.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Frogetech.MODID);

    public static final Supplier<CreativeModeTab> FROGETECH_ITEMS_TAB = CREATIVE_MODE_TAB.register("frogetech_items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.GUNK_BALL.get()))
                    .title(Component.translatable("creativetab.frogetech.frogetech_items"))
                    .displayItems(((itemDisplayParameters, output) -> {
                        output.accept(ModItems.GUNK_BALL);
                        output.accept(ModItems.ADVANCED_UPGRADE);
                        output.accept(ModItems.ELITE_UPGRADE);
                    })).build());

    public static final Supplier<CreativeModeTab> FROGETECH_BLOCKS_TAB = CREATIVE_MODE_TAB.register("frogetech_blocks_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.SOLID_GUNK))
                    .title(Component.translatable("creativetab.frogetech.frogetech_blocks"))
                    .displayItems(((itemDisplayParameters, output) -> {
                        output.accept(ModBlocks.SOLID_GUNK);
                        output.accept(ModBlocks.PEDESTAL.get());
                        output.accept(ModBlocks.TOAD_TOASTER.get());
                        output.accept(ModBlocks.CABLE.get());
                        output.accept(ModBlocks.COAL_GENERATOR.get());
                    })).build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
