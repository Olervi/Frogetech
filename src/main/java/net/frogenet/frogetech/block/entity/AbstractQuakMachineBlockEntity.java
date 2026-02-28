package net.frogenet.frogetech.block.entity;

import net.frogenet.frogetech.energy.QuakEnergyStorage;
import net.frogenet.frogetech.energy.network.IQuakStorageProvider;
import net.frogenet.frogetech.energy.network.QuakNetwork;
import net.frogenet.frogetech.energy.network.QuakNetworkMember;
import net.frogenet.frogetech.tier.MachineTier;
import net.frogenet.frogetech.tier.UpgradeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public abstract class AbstractQuakMachineBlockEntity extends BlockEntity implements QuakNetworkMember, IQuakStorageProvider {

    public QuakEnergyStorage energyStorage;
    //Current Tier read from Upgrade Slot
    protected MachineTier currentTier = MachineTier.BASIC;    //Upgrade Slot - 1 Slot for Upgrade Item
    public ItemStackHandler upgradeSlot = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return UpgradeItem.isUpgradeItem(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            updateTier();
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    public AbstractQuakMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        initEnergyStorage();
    }

    @Nullable
    private QuakNetwork network;

    protected void updateTier() {
        MachineTier newTier = UpgradeItem.getTierForItem(upgradeSlot.getStackInSlot(0));
        if (newTier == currentTier) return;

        int oldEnergy = energyStorage != null ? energyStorage.getEnergy() : 0;
        currentTier = newTier;
        initEnergyStorage();

        energyStorage.setEnergy(Math.min(oldEnergy, energyStorage.getMaxEnergy()));
    }

    private void initEnergyStorage() {
        energyStorage = new QuakEnergyStorage(
                getMaxBuffer(), getMaxInsert(), getMaxExtract()) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
                if (level != null && !level.isClientSide()) {
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                }
            }
        };
    }

    public MachineTier getCurrentTier() {
        return currentTier;
    }

    public abstract int getMaxTransfer();

    public abstract int getMaxBuffer();

    public abstract int getMaxInsert();

    public abstract int getMaxExtract();

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("energy", energyStorage.serializeNBT());
        tag.put("upgradeSlot", upgradeSlot.serializeNBT(registries));
    }

    @Override
    public QuakEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public BlockPos getNetworkPos() {
        return worldPosition;
    }

    @Override
    public Level getNetworkLevel() {
        return level;
    }

    @Override
    public void onJoinNetwork(QuakNetwork network) {
        this.network = network;
    }

    @Override
    public void onLeaveNetwork() {
        this.network = null;
    }

    @Nullable
    @Override
    public QuakNetwork getNetwork() {
        return network;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        upgradeSlot.deserializeNBT(registries, tag.getCompound("upgradeSlot"));
        updateTier();
        energyStorage.deserializeNBT(tag.getCompound("energy"));
    }


}
