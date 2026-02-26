package net.frogenet.frogetech.datagen;

import net.frogenet.frogetech.Frogetech;
import net.frogenet.frogetech.block.ModBlocks;
import net.frogenet.frogetech.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, Frogetech.MODID, helper);
    }

    @Override
    protected void registerModels() {
        withExistingParent(ModBlocks.TOAD_TOASTER.getId().getPath(),
                modLoc("block/toad_toaster"));
        withExistingParent(ModBlocks.COAL_GENERATOR.getId().getPath(),
                modLoc("block/coal_generator"));
        withExistingParent(ModBlocks.CABLE.getId().getPath(),
                modLoc("block/cable_core"));


        basicItem(ModItems.GUNK_BALL.get());
    }
}
