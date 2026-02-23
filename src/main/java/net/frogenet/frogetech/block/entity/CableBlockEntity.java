package net.frogenet.frogetech.block.entity;

import net.frogenet.frogetech.energy.network.QuakNetwork;
import net.frogenet.frogetech.energy.network.QuakNetworkMember;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class CableBlockEntity extends BlockEntity implements QuakNetworkMember {
    private static final int MAX_TRANSFER = 100;

    @Nullable
    private QuakNetwork network;

    public CableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CABLE_BE.get(), pos, state);
    }

    //QNM

    @Override
    public BlockPos getNetworkPos(){ return worldPosition; }

    @Override
    public Level getNetworkLevel() {return level; }

    @Override
    public void onJoinNetwork(QuakNetwork network) {
        this.network = network;
    }

    @Override
    public void onLeaveNetwork() {
        this.network = null;
    }

    @Override
    @Nullable
    public QuakNetwork getNetwork() {
        return network;
    }

    @Override
    public boolean isConductor() {return true; }

    @Override
    public int getMaxTransfer() {
        return MAX_TRANSFER;
    }
}
