package net.frogenet.frogetech.datagen;

import net.frogenet.frogetech.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModLootTableProvider extends LootTableProvider {

    public ModLootTableProvider(PackOutput output,
                                CompletableFuture<HolderLookup.Provider> provider) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(ModBlockLoot::new,
                        LootContextParamSets.BLOCK)
        ), provider);
    }

    static class ModBlockLoot extends BlockLootSubProvider {
        protected ModBlockLoot(HolderLookup.Provider provider) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
        }

        @Override
        protected void generate() {
            dropSelf(ModBlocks.TOAD_TOASTER.get());
            dropSelf(ModBlocks.CABLE.get());
            dropSelf(ModBlocks.COAL_GENERATOR.get());
            dropSelf(ModBlocks.SOLID_GUNK.get());
            dropSelf(ModBlocks.PEDESTAL.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ModBlocks.BLOCKS.getEntries()
                    .stream()
                    .map(holder -> (Block) holder.get())
                    .toList();
        }
    }
}
