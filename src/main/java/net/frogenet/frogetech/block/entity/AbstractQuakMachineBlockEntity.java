package net.frogenet.frogetech.block.entity;

import net.frogenet.frogetech.energy.QuakEnergyStorage;
import net.frogenet.frogetech.energy.network.IQuakStorageProvider;
import net.frogenet.frogetech.energy.network.QuakNetwork;
import net.frogenet.frogetech.energy.network.QuakNetworkMember;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public abstract class AbstractQuakMachineBlockEntity extends BlockEntity implements QuakNetworkMember, IQuakStorageProvider {

    public final QuakEnergyStorage energyStorage;

    @Nullable
    private QuakNetwork network;

    public AbstractQuakMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.energyStorage = new QuakEnergyStorage(
                getMaxBuffer(), getMaxInsert(), getMaxExtract()) {
            protected void onEnergyChange() {
                setChanged();
                if (level != null && !level.isClientSide()) {
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                }
            }
        };
    }

    public abstract int getMaxBuffer();

    public abstract int getMaxInsert();

    public abstract int getMaxExtract();

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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("energy", energyStorage.serializeNBT());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energyStorage.deserializeNBT(tag.getCompound("energy"));
    }
}
