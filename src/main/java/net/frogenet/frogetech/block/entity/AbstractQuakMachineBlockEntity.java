package net.frogenet.frogetech.block.entity;

import net.frogenet.frogetech.energy.QuakEnergyStorage;
import net.frogenet.frogetech.energy.network.IQuakStorageProvider;
import net.frogenet.frogetech.energy.network.QuakNetwork;
import net.frogenet.frogetech.energy.network.QuakNetworkMember;
import net.frogenet.frogetech.entity.custom.FrogEntity;
import net.frogenet.frogetech.tier.MachineTier;
import net.frogenet.frogetech.tier.UpgradeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.UUID;

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
    private UUID sittingFrogUUID = null;
    private float sittingFrogYRot = 0f;

    @Nullable
    private QuakNetwork network;

    private static boolean isSittableFrog(Entity entity) {
        return entity instanceof FrogEntity || entity instanceof Frog;
    }

    public boolean hasSittingFrog() {
        return sittingFrogUUID != null;
    }

    public void setSittingFrog(Mob mob, float yRot) {
        this.sittingFrogUUID = mob.getUUID();
        this.sittingFrogYRot = yRot;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void releaseSittingFrog(Level level) {
        if (sittingFrogUUID == null) return;
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(sittingFrogUUID);
            if (entity instanceof FrogEntity modFrog) {
                modFrog.clearSittingPos();
            } else if (entity instanceof Mob mob) {
                mob.setNoAi(false);
                mob.getPersistentData().remove("FrogSittingX");
                mob.getPersistentData().remove("FrogSittingY");
                mob.getPersistentData().remove("FrogSittingZ");
            }
        }
        sittingFrogUUID = null;
        setChanged();
    }

    public void tickFrogSitting(Level level, BlockPos pos) {
        if (sittingFrogUUID == null) return;
        if (level.getGameTime() % 20 != 0) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        Entity entity = serverLevel.getEntity(sittingFrogUUID);
        if (!isSittableFrog(entity)) {
            sittingFrogUUID = null;
            setChanged();
            return;
        }

        Mob mob = (Mob) entity;
        double targetX = pos.getX() + 0.5;
        double targetY = pos.getY() + 1.0;
        double targetZ = pos.getZ() + 0.5;

        if (mob.distanceToSqr(targetX, targetY, targetZ) > 0.5
                || Math.abs(mob.getYRot() - sittingFrogYRot) > 5f) {
            mob.moveTo(targetX, targetY, targetZ, sittingFrogYRot, 0f);
        }
    }

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
        if (sittingFrogUUID != null) {
            tag.putUUID("sittingFrogUUID", sittingFrogUUID);
            tag.putFloat("sittingFrogYRot", sittingFrogYRot);
        }
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
        if (tag.hasUUID("sittingFrogUUID")) {
            sittingFrogUUID = tag.getUUID("sittingFrogUUID");
            sittingFrogYRot = tag.getFloat("sittingFrogYRot");
        }
    }


}
