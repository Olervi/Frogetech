package net.frogenet.frogetech.energy.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

    /**
     * Side-aware connection rule for cables.
     * The side is relative to this block entity.
     * <p>
     * Default is permissive to keep behavior unchanged, but machines can override to expose only specific faces.
     */
    default boolean canConnectFrom(Direction side) {
        return true;
    }

    int getMaxTransfer();
}
