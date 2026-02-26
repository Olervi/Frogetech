package net.frogenet.frogetech.block.entity;

import net.frogenet.frogetech.screen.custom.CoalGeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class CoalGeneratorBlockEntity extends AbstractQuakMachineBlockEntity implements MenuProvider {

    private static final int TIER_MAX_BUFFER = 10_000;
    private static final int TIER_MAX_INSERT = 0;
    private static final int TIER_MAX_EXTRACT = 20;
    private static final int TIER_MAX_TRANSFER = 20;

    public final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getBurnTime(null) > 0;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private int burnTime = 0;
    private int maxBurnTime = 0;
    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int i) {
            return switch (i) {
                case 0 -> burnTime;
                case 1 -> maxBurnTime;
                case 2 -> energyStorage.getEnergy();
                case 3 -> energyStorage.getMaxEnergy();
                default -> 0;
            };
        }

        @Override
        public void set(int i, int value) {
            switch (i) {
                case 0 -> burnTime = value;
                case 1 -> maxBurnTime = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public CoalGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COAL_GENERATOR_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state,
                            CoalGeneratorBlockEntity be) {
        if (level.isClientSide) {
            return;
        }

        boolean wasActive = be.isActive();

        if (be.burnTime <= 0 && !be.energyStorage.isFull()) {
            be.tryConsumeFuel();
        }

        if (be.burnTime > 0) {
            be.burnTime--;
            be.energyStorage.insertEnergyInternal(TIER_MAX_EXTRACT);
            setChanged(level, pos, state);
        }

        if (wasActive != be.isActive()) {
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    @Override
    public int getMaxBuffer() {
        return TIER_MAX_BUFFER;
    }

    @Override
    public int getMaxInsert() {
        return TIER_MAX_INSERT;
    }

    @Override
    public int getMaxExtract() {
        return TIER_MAX_EXTRACT;
    }

    @Override
    public int getMaxTransfer() {
        return TIER_MAX_TRANSFER;
    }

    @Override
    public boolean isProducer() {
        return true;
    }

    private void tryConsumeFuel() {
        ItemStack fuelStack = inventory.getStackInSlot(0);
        if (fuelStack.isEmpty()) {
            return;
        }

        int burn = fuelStack.getBurnTime(null);
        if (burn <= 0) return;

        burnTime = burn;
        maxBurnTime = burn;

        ItemStack remainder = fuelStack.getCraftingRemainingItem();
        inventory.extractItem(0, 1, false);
        if (!remainder.isEmpty()) {
            inventory.setStackInSlot(0, remainder);
        }

        setChanged();

    }

    //GUI Methods

    public boolean isActive() {
        return burnTime > 0;
    }

    public float getBurnProgress() {
        if (maxBurnTime == 0) return 0f;
        return (float) burnTime / maxBurnTime;
    }

    public float getEnergyProgress() {
        if (energyStorage.getMaxEnergy() == 0) return 0f;
        return (float) energyStorage.getEnergy() / energyStorage.getMaxEnergy();
    }

    public int getEnergy() {
        return energyStorage.getEnergy();
    }

    public int getMaxEnergy() {
        return energyStorage.getMaxEnergy();
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(inventory.getSlots());
        for (int i = 0; i < inventory.getSlots(); i++) {
            inv.setItem(i, inventory.getStackInSlot(i));
        }
        Containers.dropContents(level, worldPosition, inv);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.frogetech.coal_generator");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CoalGeneratorMenu(id, inventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("burnTime", burnTime);
        tag.putInt("maxBurnTime", maxBurnTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        burnTime = tag.getInt("burnTime");
        maxBurnTime = tag.getInt("maxBurnTime");
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
