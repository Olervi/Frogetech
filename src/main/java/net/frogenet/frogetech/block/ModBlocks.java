package net.frogenet.frogetech.block;

import net.frogenet.frogetech.Frogetech;
import net.frogenet.frogetech.item.ModItems;
import net.frogenet.frogetech.block.custom.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Frogetech.MODID);

    public static final DeferredBlock<Block> SOLID_GUNK = registerBlock("solid_gunk",
            (properties -> new Block(properties
                    .strength(1f)
                    .sound(SoundType.SLIME_BLOCK))));



    public static final DeferredBlock<Block> PEDESTAL = registerBlock("pedestal",
            (properties -> new PedestalBlock(properties.noOcclusion())));

    public static final DeferredBlock<Block> TOAD_TOASTER = registerBlock("toad_toaster",
            (properties -> new ToadToasterBlock(properties.noOcclusion())));





    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> function) {
        DeferredBlock<T> toReturn = BLOCKS.registerBlock(name, function);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.registerItem(name, (properties) -> new BlockItem(block.get(), properties));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
