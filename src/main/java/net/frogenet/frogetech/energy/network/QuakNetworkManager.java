package net.frogenet.frogetech.energy.network;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

public class QuakNetworkManager {

    private static final Map<Level, QuakNetworkManager> INSTANCES = new WeakHashMap<>();

    private final Map<UUID, QuakNetwork> networks = new HashMap<>();

    private final Map<BlockPos, UUID> posToNetwork = new HashMap<>();

    private final Level level;

    private QuakNetworkManager(Level level) {
        this.level = level;
    }

    public static QuakNetworkManager get(Level level) {
        return INSTANCES.computeIfAbsent(level, QuakNetworkManager::new);
    }

    //Public API

    public void onMemberAdded(QuakNetworkMember newMember) {
        BlockPos pos = newMember.getNetworkPos();
        List<QuakNetwork> neighborNetworks = getNeighborNetworks(pos, newMember);
        if (neighborNetworks.isEmpty()) {
            QuakNetwork network = new QuakNetwork();
            network.addMember(newMember);
            registerNetwork(network);
        } else {
            QuakNetwork primary = getLargestNetwork(neighborNetworks);
            primary.addMember(newMember);
            posToNetwork.put(pos, primary.getId());

            for (QuakNetwork other : neighborNetworks){
                if (other != primary){
                    for (BlockPos memberPos : other.getMemberPositions()) {
                        posToNetwork.put(memberPos, primary.getId());
                    }
                    networks.remove(other.getId());
                    primary.mergeWith(other);
                }
            }
        }
    }

    public void onMemberRemoved(BlockPos pos) {
        UUID networkId = posToNetwork.remove(pos);
        if (networkId == null) return;

        QuakNetwork network = networks.get(networkId);
        if (network == null) return;

        network.removeMember(pos);

        if (network.getMemberCount() == 0) {
            networks.remove(networkId);
            return;
        }
        checkForSplit(network, pos);
    }
    public QuakNetwork getNetworkAt(BlockPos pos) {
        UUID id = posToNetwork.get(pos);
        return id != null ? networks.get(id) : null;
    }

    public void tick() {
        // Iterate over a snapshot in case networks are added/removed due to block updates during ticking.
        for (QuakNetwork network : new ArrayList<>(networks.values())) {
            network.tick();
        }
    }


    //Helper Methods

    private void registerNetwork(QuakNetwork network) {
        networks.put(network.getId(), network);
        for (QuakNetworkMember member : network.getAllMembers()) {
            posToNetwork.put(member.getNetworkPos(), network.getId());
        }
    }

    private List<QuakNetwork> getNeighborNetworks(BlockPos pos, QuakNetworkMember newMember) {
        Set<UUID> seen = new HashSet<>();
        List<QuakNetwork> result = new ArrayList<>();

        for (Direction dir : Direction.values()) {
            // Check if the new member wants to connect on this face.
            if (!newMember.canConnectFrom(dir)) continue;

            BlockPos neighborPos = pos.relative(dir);

            // Neighbor must exist and accept a connection on the opposite face.
            BlockEntity neighborBe = level.getBlockEntity(neighborPos);
            if (!(neighborBe instanceof QuakNetworkMember neighborMember)) continue;
            if (!neighborMember.canConnectFrom(dir.getOpposite())) continue;

            UUID id = posToNetwork.get(neighborPos);
            if (id != null && seen.add(id)) {
                QuakNetwork net = networks.get(id);
                if (net != null) result.add(net);
            }
        }
        return result;
    }

    private QuakNetwork getLargestNetwork(List<QuakNetwork> nets) {
        return nets.stream()
                .max(Comparator.comparingInt(QuakNetwork::getMemberCount))
                .orElseThrow();
    }

    private void checkForSplit (QuakNetwork oldNetwork, BlockPos removedPos) {
        Set<BlockPos> allPositions = new HashSet<>(oldNetwork.getMemberPositions());
        List<Set<BlockPos>> components = findConnectedComponents(allPositions);

        if(components.size() <= 1) return;

        UUID oldId = oldNetwork.getId();
        networks.remove(oldId);

        for(Set<BlockPos> component : components){
            QuakNetwork newNetwork = new QuakNetwork();
            for(BlockPos memberPos : component) {
                BlockEntity be = level.getBlockEntity(memberPos);
                if (be instanceof QuakNetworkMember member) {
                    newNetwork.addMember(member);
                    posToNetwork.put(memberPos, newNetwork.getId());
                }
            }
            networks.put(newNetwork.getId(), newNetwork);
        }
    }

    private List<Set<BlockPos>> findConnectedComponents(Set<BlockPos> positions) {
        Set<BlockPos> unvisited = new HashSet<>(positions);
        List<Set<BlockPos>> components = new ArrayList<>();

        while (!unvisited.isEmpty()) {
            BlockPos start = unvisited.iterator().next();
            Set<BlockPos> component = new HashSet<>();
            Queue<BlockPos> queue = new LinkedList<>();
            queue.add(start);

            while (!queue.isEmpty()) {
                BlockPos current = queue.poll();
                if(!unvisited.remove(current)) continue;
                component.add(current);

                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = current.relative(dir);
                    if(unvisited.contains(neighbor)) {
                        queue.add(neighbor);
                    }
                }
            }
            components.add(component);
        }
        return components;
    }

    //Cleanup

    public static void onLevelUnload (Level level) {
        INSTANCES.remove(level);
    }
}
