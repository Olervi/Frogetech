package net.frogenet.frogetech.block.entity;

import net.frogenet.frogetech.screen.custom.ToadToasterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ToadToasterBlockEntity extends AbstractQuakMachineBlockEntity implements MenuProvider {

    //Tiered Values
    private static final int TIER_MAX_BUFFER = 5_000;
    private static final int TIER_MAX_INSERT = 40;
    private static final int TIER_MAX_EXTRACT = 0;
    private static final int TIER_MAX_TRANSFER = 40;
    private static final int ENERGY_PER_TICK = 20;

    public final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
            if (slot == INPUT_SLOT) {
                cookingProgress = 0;
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack){
            return switch (slot) {
                case INPUT_SLOT -> true;
                case OUTPUT_SLOT -> false;
                default -> super.isItemValid(slot, stack);
            };
        }
    };

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    private int cookingProgress = 0;
    private int cookingTotalTime = 200;
    private ItemStack lastInput = ItemStack.EMPTY;

    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int i) {
            return switch (i) {
                case 0 -> cookingProgress;
                case 1 -> cookingTotalTime;
                case 2 -> energyStorage.getEnergy();
                case 3 -> energyStorage.getMaxEnergy();
                default -> 0;
            };
        }

        @Override
        public void set(int i, int value) {
            switch (i) {
                case 0 -> cookingProgress = value;
                case 1 -> cookingTotalTime = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public ToadToasterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOAD_TOASTER_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state,
                            ToadToasterBlockEntity blockEntity) {
        if (level.isClientSide()) return;

        blockEntity.tickServer(level);
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
    public boolean isConsumer() {
        return true;
    }

    private void tickServer(Level level) {
        ItemStack inputStack = itemHandler.getStackInSlot(INPUT_SLOT);

        // Kein Input? Reset!
        if (inputStack.isEmpty()) {
                cookingProgress = 0;
                setChanged();
            return;
        }

        if (!ItemStack.isSameItemSameComponents(inputStack, lastInput)) {
            cookingProgress = 0;
            lastInput = inputStack.copy();
            setChanged();
        }

        SingleRecipeInput recipeInput = new SingleRecipeInput(inputStack);
        Optional<RecipeHolder<SmeltingRecipe>> recipeHolder = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, recipeInput, level);

        if (recipeHolder.isEmpty()) {
            if (cookingProgress > 0) {
                cookingProgress = 0;
                setChanged();
            }
            return;
        }

        SmeltingRecipe recipe = recipeHolder.get().value();
        cookingTotalTime = recipe.getCookingTime();

        if (!canCook(recipe, recipeInput)) {
            if (cookingProgress > 0) {
                cookingProgress = Math.max(0, cookingProgress - 2);
                setChanged();
            }
            return;
        }

        if (energyStorage.getEnergy() < ENERGY_PER_TICK) {
            setChanged();
            return;
        }

        energyStorage.setEnergy(
                Math.max(0, energyStorage.getEnergy() - ENERGY_PER_TICK)
        );
        cookingProgress++;
        setChanged();

        if (cookingProgress >= cookingTotalTime) {
            cook(recipe, recipeInput);
            cookingProgress = 0;
            lastInput = itemHandler.getStackInSlot(INPUT_SLOT).copy();
            setChanged();
        }
    }

    private boolean canCook(SmeltingRecipe recipe, SingleRecipeInput input) {
        ItemStack result = recipe.assemble(input, level.registryAccess());
        ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);

        if (result.isEmpty()) return false;
        if (outputStack.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(outputStack, result)) return false;
        return outputStack.getCount() + result.getCount() <= outputStack.getMaxStackSize();
    }

    private void cook(SmeltingRecipe recipe, SingleRecipeInput input) {
        ItemStack result = recipe.assemble(input, level.registryAccess());
        ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);

        // Input verbrauchen
        itemHandler.extractItem(INPUT_SLOT, 1, false);

        // Output hinzufügen
        if (outputStack.isEmpty()) {
            itemHandler.setStackInSlot(OUTPUT_SLOT, result.copy());
        } else {
            outputStack.grow(result.getCount());
        }
    }

    public boolean isCooking() {
        return cookingProgress > 0;
    }



    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.putInt("progress", cookingProgress);
        tag.putInt("total_time", cookingTotalTime);
        tag.put("last_input", lastInput.saveOptional(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        cookingProgress = tag.getInt("progress");
        cookingTotalTime = tag.getInt("total_time");
        lastInput = ItemStack.parseOptional(registries, tag.getCompound("last_input"));

    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public int getCookingProgress() {
        return cookingProgress;
    }

    public int getCookingTotalTime() {
        return cookingTotalTime;
    }


    @Override
    public Component getDisplayName() {
        return Component.translatable("block.frogetech.toad_toaster");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ToadToasterMenu(id, inventory, this, this.data);
    }

    public void drops(){
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(level, worldPosition, inventory);
    }
}
