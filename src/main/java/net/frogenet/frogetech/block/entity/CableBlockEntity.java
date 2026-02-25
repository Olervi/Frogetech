package net.frogenet.frogetech.block.entity;

import net.frogenet.frogetech.energy.network.QuakNetwork;
import net.frogenet.frogetech.energy.network.QuakNetworkMember;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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

    private int cachedThroughput = 0;

    public int getCachedThroughput() {
        return cachedThroughput;
    }

    public void setCachedThroughput(int throughput) {
        this.cachedThroughput = throughput;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("throughput", cachedThroughput);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        cachedThroughput = tag.getInt("throughput");
    }

    @Override
    public @org.jetbrains.annotations.Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
