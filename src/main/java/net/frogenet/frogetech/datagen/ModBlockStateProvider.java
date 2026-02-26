package net.frogenet.frogetech.datagen;

import net.frogenet.frogetech.Frogetech;
import net.frogenet.frogetech.block.ModBlocks;
import net.frogenet.frogetech.block.custom.CableBlock;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, Frogetech.MODID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        //Solid Gunk
        simpleBlockWithItem(ModBlocks.SOLID_GUNK.get(), cubeAll(ModBlocks.SOLID_GUNK.get()));

        //Toad Toaster
        simpleBlockWithItem(ModBlocks.TOAD_TOASTER.get(),
                cubeAll(ModBlocks.TOAD_TOASTER.get()));

        //Coal Gen

        simpleBlockWithItem(ModBlocks.COAL_GENERATOR.get(),
                cubeAll(ModBlocks.COAL_GENERATOR.get()));

        //Cable

        cableBlock(ModBlocks.CABLE.get());
    }


    private void cableBlock(Block block) {
        ResourceLocation coreModel = modLoc("block/cable_core");
        ResourceLocation armModel = modLoc("block/cable_arm");

        MultiPartBlockStateBuilder builder = getMultipartBuilder(block);

        //Core

        builder.part()
                .modelFile(models().getExistingFile(coreModel))
                .addModel()
                .end();

        // North
        builder.part()
                .modelFile(models().getExistingFile(armModel))
                .rotationX(90).rotationY(0)
                .addModel()
                .condition(CableBlock.NORTH, true)
                .end();

        // South
        builder.part()
                .modelFile(models().getExistingFile(armModel))
                .rotationX(90).rotationY(180)
                .addModel()
                .condition(CableBlock.SOUTH, true)
                .end();

        // East
        builder.part()
                .modelFile(models().getExistingFile(armModel))
                .rotationX(90).rotationY(90)
                .addModel()
                .condition(CableBlock.EAST, true)
                .end();

        // West
        builder.part()
                .modelFile(models().getExistingFile(armModel))
                .rotationX(90).rotationY(270)
                .addModel()
                .condition(CableBlock.WEST, true)
                .end();

        // Up
        builder.part()
                .modelFile(models().getExistingFile(armModel))
                .rotationX(0).rotationY(0)
                .addModel()
                .condition(CableBlock.UP, true)
                .end();

        // Down
        builder.part()
                .modelFile(models().getExistingFile(armModel))
                .rotationX(180).rotationY(0)
                .addModel()
                .condition(CableBlock.DOWN, true)
                .end();
    }
}

