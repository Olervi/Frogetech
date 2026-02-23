package net.frogenet.frogetech.energy;

import net.minecraft.nbt.CompoundTag;

public class QuakEnergyStorage implements IQuakStorage{

    protected int energy;
    protected final int capacity;
    protected final int maxInsert;
    protected final int maxExtract;

    public QuakEnergyStorage(int capacity, int maxInsert, int maxExtract) {
        this.capacity = capacity;
        this.maxInsert = maxInsert;
        this.maxExtract = maxExtract;
        this.energy = 0;
    }

    public QuakEnergyStorage(int capacity, int maxTransfer) {
        this(capacity, maxTransfer, maxTransfer);
    }

    @Override
    public int insertEnergy(int amount, boolean simulate) {
       if (!canReceive() || amount <= 0) return 0;
       int accepted = Math.min(capacity - energy, Math.min(maxInsert, amount));
       if(!simulate){
           energy += accepted;
           onEnergyChanged();
       }
       return accepted;
    }

    @Override
    public int extractEnergy(int amount, boolean simulate) {
        if (!canExtract() || amount <= 0) return 0;
        int extracted = Math.min(energy, Math.min(maxExtract, amount));
        if (!simulate) {
            energy -= extracted;
            onEnergyChanged();
        }
        return extracted;
    }

    @Override
    public int getEnergy() {
        return energy;
    }

    @Override
    public int getMaxEnergy() {
        return capacity;
    }

    protected void onEnergyChanged() {}

    //NBT

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("energy", energy);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        energy = Math.min(tag.getInt("energy"), capacity);
    }

    //Only for Testing
    public void setEnergy(int energy) {
        this.energy = Math.min(energy, capacity);
    }
}
