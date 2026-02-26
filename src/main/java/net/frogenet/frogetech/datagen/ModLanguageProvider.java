package net.frogenet.frogetech.datagen;

import net.frogenet.frogetech.Frogetech;
import net.frogenet.frogetech.block.ModBlocks;
import net.frogenet.frogetech.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output) {
        super(output, Frogetech.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(ModBlocks.TOAD_TOASTER.get(), "Toad Toaster");
        add(ModBlocks.COAL_GENERATOR.get(), "Coal Generator");
        add(ModBlocks.CABLE.get(), "Quak Cable");

        add(ModBlocks.SOLID_GUNK.get(), "Solid Gunk");

        add(ModItems.GUNK_BALL.get(), "Gunk Ball");

        add("creativetab.frogetech.frogetech_items", "Frogetech Items");
        add("creativetab.frogetech.frogetech_blocks", "Frogetech Blocks");
    }
}
