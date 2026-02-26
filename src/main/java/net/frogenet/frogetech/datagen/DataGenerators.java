package net.frogenet.frogetech.datagen;


import net.frogenet.frogetech.Frogetech;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Frogetech.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();


        // Client Datagen
        generator.addProvider(event.includeClient(),
                new ModBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(),
                new ModItemModelProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(),
                new ModLanguageProvider(output));

        // Server Datagen
        generator.addProvider(event.includeServer(),
                new ModLootTableProvider(output, lookupProvider));
        generator.addProvider(event.includeServer(),
                new ModRecipeProvider(output, lookupProvider));
        generator.addProvider(event.includeServer(),
                new ModBlockTagProvider(output, lookupProvider, event.getExistingFileHelper()));
    }
}
