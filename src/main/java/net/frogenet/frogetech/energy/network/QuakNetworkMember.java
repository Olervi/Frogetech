package net.frogenet.frogetech.energy.network;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface QuakNetworkMember {
    BlockPos getNetworkPos();
    Level getNetworkLevel();

    void onJoinNetwork(QuakNetwork network);

    void onLeaveNetwork();

    QuakNetwork getNetwork();

    default boolean isProducer() { return false; }

    default boolean isConsumer() { return false; }

    default boolean isConductor() { return false; }

    int getMaxTransfer();
}
