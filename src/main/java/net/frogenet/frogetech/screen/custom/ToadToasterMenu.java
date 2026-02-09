package net.frogenet.frogetech.screen.custom;

import net.frogenet.frogetech.block.ModBlocks;
import net.frogenet.frogetech.block.entity.ToadToasterBlockEntity;
import net.frogenet.frogetech.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ToadToasterMenu extends AbstractContainerMenu {

    private final ToadToasterBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;


    public ToadToasterMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(3));
    }

    public ToadToasterMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.TOAD_TOASTER_MENU.get(), pContainerId);

        checkContainerSize(inv, 3);

        this.blockEntity = ((ToadToasterBlockEntity) entity);
        this.level = inv.player.level();
        this.data = data;

        addPlayerHotbar(inv);
        addPlayerInventory(inv);

        //INPUT SLOT
        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 0, 44, 18));
        //OUTPUT SLOT
        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 1, 107, 37));

        //FUEL SLOt

        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 2, 44, 56));

        addDataSlots(data);
    }
    public boolean isCrafting(){
        return data.get(0) > 0;
    }

    public int getScaledArrowProgress(){
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        int arrowPixelSize = 24;
        return maxProgress != 0 && progress != 0 ? progress * arrowPixelSize / maxProgress : 0;
    }

    public boolean isEnabled() {
        return data.get(2) > 0;
    }


    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;

    private static final int VANILLA_FIRST_SLOT_INDEX = 0;

    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_SLOT_COUNT;
    private static final int TE_INVENTORY_SLOT_COUNT = 3;


    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if(pIndex >= TE_INVENTORY_FIRST_SLOT_INDEX && pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT){
            if(!moveItemStackTo(sourceStack, 0, HOTBAR_SLOT_COUNT, false)) {
                if(!moveItemStackTo(sourceStack, HOTBAR_SLOT_COUNT, VANILLA_SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else if (pIndex >= VANILLA_FIRST_SLOT_INDEX && pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
                if(!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX + 1, false)) {
                    if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX + 2, TE_INVENTORY_FIRST_SLOT_INDEX + 3, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        else {
            System.out.println("Invalid slotIndex:" + pIndex);
            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;

    }


    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ModBlocks.TOAD_TOASTER.get());
    }

    private void addPlayerInventory(Inventory playerInventory){
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j+i * 9+9, 8+j*18, 84+i*18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory){
        for(int i = 0; i< 9; ++i){
            this.addSlot(new Slot(playerInventory, i, 8+i * 18, 142));
        }
    }
}
