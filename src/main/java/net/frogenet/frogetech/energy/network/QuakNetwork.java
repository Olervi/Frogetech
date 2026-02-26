package net.frogenet.frogetech.energy.network;

import net.frogenet.frogetech.block.entity.CableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.*;

public class QuakNetwork {

    private final UUID id;

    private final Map<BlockPos, QuakNetworkMember> members = new HashMap<>();

    public QuakNetwork() {
        this.id = UUID.randomUUID();
    }

    private int lastTransferredAmount = 0;

    private QuakNetwork(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    // Member Management
    public void addMember(QuakNetworkMember member) {
        members.put(member.getNetworkPos(), member);
        member.onJoinNetwork(this);
    }

    public void removeMember(BlockPos pos) {
        QuakNetworkMember member = members.remove(pos);
        if (member != null) {
            member.onLeaveNetwork();
        }
    }

    public boolean hasMember(BlockPos pos) {
        return members.containsKey(pos);
    }

    public QuakNetworkMember getMember(BlockPos pos) {
        return members.get(pos);
    }

    public Collection<QuakNetworkMember> getAllMembers() {
        return Collections.unmodifiableCollection(members.values());
    }

    public int getMemberCount() {
        return members.size();
    }

    //Energy Tick

    public void tick() {
        int networkTransferLimit = getNetworkTransferLimit();
        lastTransferredAmount = 0;

        if (networkTransferLimit <= 0) {
            updateCableThroughput();
            return;
        }

        int availableEnergy = 0;
        List<QuakNetworkMember> producers = new ArrayList<>();
        for (QuakNetworkMember member : members.values()) {
            if (member.isProducer()) {
                producers.add(member);
                IQuakStorageProvider provider = (IQuakStorageProvider) member;
                availableEnergy += provider.getEnergyStorage()
                        .extractEnergy(networkTransferLimit, true);
            }
        }

        if (availableEnergy <= 0) {
            updateCableThroughput();
            return;
        }

        List<QuakNetworkMember> consumers = new ArrayList<>();
        int totalDemand = 0;
        for (QuakNetworkMember member : members.values()) {
            if (member.isConsumer()) {
                IQuakStorageProvider provider = (IQuakStorageProvider) member;
                int canReceive = provider.getEnergyStorage()
                        .insertEnergy(availableEnergy, true);
                if (canReceive > 0) {
                    consumers.add(member);
                    totalDemand += canReceive;
                }
            }
        }
        if (totalDemand <= 0) return;

        int toDistribute = Math.min(availableEnergy, totalDemand);
        lastTransferredAmount = toDistribute;

        int remainingToExtract = toDistribute;
        for (QuakNetworkMember producer : producers) {
            if (remainingToExtract <= 0) break;
            IQuakStorageProvider provider = (IQuakStorageProvider) producer;
            remainingToExtract -= provider.getEnergyStorage()
                    .extractEnergy(remainingToExtract, false);
        }

        int remainingToInsert = toDistribute;
        for (QuakNetworkMember consumer : consumers) {
            if (remainingToInsert <= 0) break;
            IQuakStorageProvider provider = (IQuakStorageProvider) consumer;
            int share = (int) Math.ceil((double) remainingToInsert / consumers.size());
            remainingToInsert -= provider.getEnergyStorage()
                    .insertEnergy(share, false);
        }

        lastTransferredAmount = toDistribute;
        updateCableThroughput();
    }

    private void updateCableThroughput() {
        for (QuakNetworkMember member : members.values()) {
            if (member.isConductor() && member instanceof CableBlockEntity cable) {
                cable.setCachedThroughput(lastTransferredAmount);
                Level level = cable.getNetworkLevel();
                BlockPos cablePos = cable.getNetworkPos();
                if (level != null) {
                    level.sendBlockUpdated(cablePos,
                            level.getBlockState(cablePos),
                            level.getBlockState(cablePos), 3);
                }
            }
        }
    }

    private int getNetworkTransferLimit() {
        int min = Integer.MAX_VALUE;
        boolean hasConductor = false;
        for (QuakNetworkMember member : members.values()) {
            if (member.isConductor()) {
                hasConductor = true;
                min = Math.min(min, member.getMaxTransfer());
            }
        }
        return hasConductor ? min : Integer.MAX_VALUE;
    }

    public int getLastTransferredAmount() {
        return lastTransferredAmount;
    }

    //Net Merge

    public QuakNetwork mergeWith(QuakNetwork other) {
        if (other == this) return this;
        for (QuakNetworkMember member : new ArrayList<>(other.members.values())) {
            other.removeMember(member.getNetworkPos());
            this.addMember(member);
        }
        return this;
    }

    //Split

    public Set<BlockPos> getMemberPositions() {
        return Collections.unmodifiableSet(members.keySet());
    }

    @Override
    public String toString() {
        return "QuakNetwork{id=" + id.toString().substring(0, 8) +
                ", members=" + members.size() + "}";
    }
}
