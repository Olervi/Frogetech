package net.frogenet.frogetech.energy;

public interface IQuakStorage {
    int insertEnergy(int amount, boolean simulate);

    int extractEnergy(int amount, boolean simulate);

    int getEnergy();

    int getMaxEnergy();

    default boolean canReceive() {return  true;}
    default boolean canExtract() {return  true;}
    default boolean isFull() { return getEnergy() >= getMaxEnergy(); }
    default boolean isEmpty() { return getEnergy() <= 0; }
}
