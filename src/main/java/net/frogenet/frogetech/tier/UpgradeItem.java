package net.frogenet.frogetech.tier;

import net.frogenet.frogetech.item.ModItems;
import net.minecraft.world.item.ItemStack;

public class UpgradeItem {

    public static MachineTier getTierForItem(ItemStack stack) {
        if (stack.isEmpty()) return MachineTier.BASIC;
        if (stack.is(ModItems.ADVANCED_UPGRADE.get())) return MachineTier.ADVANCED;
        if (stack.is(ModItems.ELITE_UPGRADE.get())) return MachineTier.ELITE;
        return MachineTier.BASIC;
    }

    public static boolean isUpgradeItem(ItemStack stack) {
        return stack.is(ModItems.ADVANCED_UPGRADE.get()) || stack.is(ModItems.ELITE_UPGRADE.get());
    }

    public static ItemStack getItemForTier(MachineTier tier) {
        return switch (tier) {
            case ADVANCED -> new ItemStack(ModItems.ADVANCED_UPGRADE.get());
            case ELITE -> new ItemStack(ModItems.ELITE_UPGRADE.get());
            default -> ItemStack.EMPTY;
        };
    }
}
