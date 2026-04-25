package net.frogenet.frogetech.screen.custom;

import net.frogenet.frogetech.block.ModBlocks;
import net.frogenet.frogetech.block.entity.CoalGeneratorBlockEntity;
import net.frogenet.frogetech.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class CoalGeneratorMenu extends AbstractQuakMachineMenu {
    private static final int HOTBAR_SLOTS = 9;
    private static final int INV_SLOTS = 27;
    private static final int VANILLA_SLOTS = HOTBAR_SLOTS + INV_SLOTS;
    private static final int TE_FIRST_SLOT = VANILLA_SLOTS;
    private static final int TE_SLOT_COUNT = 2;
    public final CoalGeneratorBlockEntity blockEntity;
    private final ContainerData data;

    public CoalGeneratorMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos()),
                new SimpleContainerData(5));
    }

    public CoalGeneratorMenu(int id, Inventory inv, BlockEntity be, ContainerData data) {
        super(ModMenuTypes.COAL_GENERATOR_MENU.get(), id,
                (CoalGeneratorBlockEntity) be);
        this.blockEntity = (CoalGeneratorBlockEntity) be;
        this.data = data;


        addPlayerHotbar(inv);
        addPlayerInventory(inv);

        this.addSlot(new SlotItemHandler(blockEntity.inventory, 0, 80, 35));
        addUpgradeSlot();

        addDataSlots(this.data);
    }


    @Override
    public ItemStack handleQuickMove(Player player, int index) {
        Slot source = slots.get(index);
        if (source == null || !source.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = source.getItem();
        ItemStack copy = stack.copy();

        if (index < VANILLA_SLOTS) {
            if (!moveItemStackTo(stack, TE_FIRST_SLOT, TE_FIRST_SLOT + TE_SLOT_COUNT, false))
                return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stack, 0, VANILLA_SLOTS, false))
                return ItemStack.EMPTY;
        }

        if (stack.getCount() == 0) source.set(ItemStack.EMPTY);
        else source.setChanged();
        source.onTake(player, stack);
        return copy;

    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(
                        blockEntity.getLevel(), blockEntity.getBlockPos()),
                player, ModBlocks.COAL_GENERATOR.get());
    }

    private void addPlayerInventory(Inventory inv) {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 9; j++)
                addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
    }

    private void addPlayerHotbar(Inventory inv) {
        for (int i = 0; i < 9; i++)
            addSlot(new Slot(inv, i, 8 + i * 18, 142));
    }

    public float getBurnProgress() {
        int burn = data.get(0);
        int maxBurn = data.get(1);
        if (maxBurn == 0) return 0f;
        return (float) burn / maxBurn;
    }

    public float getEnergyProgress() {
        int energy = data.get(2);
        int maxEnergy = data.get(3);
        if (maxEnergy == 0) return 0f;
        return (float) energy / maxEnergy;
    }

    public int getEnergy() {
        return data.get(2);
    }

    public int getMaxEnergy() {
        return data.get(3);
    }

    public int getTierLevel() {
        return data.get(4);
    }
}
