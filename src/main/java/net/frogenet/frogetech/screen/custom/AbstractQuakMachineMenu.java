package net.frogenet.frogetech.screen.custom;

import net.frogenet.frogetech.block.entity.AbstractQuakMachineBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nullable;

public abstract class AbstractQuakMachineMenu extends AbstractContainerMenu {
    public final AbstractQuakMachineBlockEntity machineEntity;
    protected int upgradeSlotIndex = -1;

    protected AbstractQuakMachineMenu(@Nullable MenuType<?> type, int id,
                                      AbstractQuakMachineBlockEntity be) {
        super(type, id);
        this.machineEntity = be;
    }

    public boolean panelOpen = false;

    protected void addUpgradeSlot() {
        upgradeSlotIndex = slots.size();
        this.addSlot(new SlotItemHandler(machineEntity.upgradeSlot, 0,
                184, 58) {
            @Override
            public boolean isActive() {
                return panelOpen;
            }
        });
    }

    public int getUpgradeSlotIndex() {
        return upgradeSlotIndex;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return handleQuickMove(player, i);
    }

    protected abstract ItemStack handleQuickMove(Player player, int index);
}
