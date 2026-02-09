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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ToadToasterBlockEntity extends BlockEntity implements MenuProvider {
    public final ItemStackHandler itemHandler = new ItemStackHandler(3) {
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
                case FUEL_SLOT -> isFuel(stack);
                default -> super.isItemValid(slot, stack);
            };
        }
    };

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int FUEL_SLOT = 2;

    protected final ContainerData data;
    private int cookingProgress = 0;
    private int cookingTotalTime = 200;
    private ItemStack lastInput = ItemStack.EMPTY;

    private int fuelTime = 0;
    private int maxFuelTime = 0;

    public ToadToasterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOAD_TOASTER_BE.get(), pos, state);
        data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i){
                    case 0 -> ToadToasterBlockEntity.this.cookingProgress;
                    case 1 -> ToadToasterBlockEntity.this.cookingTotalTime;
                    case 2 -> ToadToasterBlockEntity.this.fuelTime;
                    case 3 -> ToadToasterBlockEntity.this.maxFuelTime;
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int value) {
                switch (i){
                    case 0 -> ToadToasterBlockEntity.this.cookingProgress = value;
                    case 1 -> ToadToasterBlockEntity.this.cookingTotalTime = value;
                    case 2 -> ToadToasterBlockEntity.this.fuelTime = value;
                    case 3 -> ToadToasterBlockEntity.this.maxFuelTime = value;
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }

    public static void tick(Level level, BlockPos pos, BlockState state,
                            ToadToasterBlockEntity blockEntity) {
        if (level.isClientSide()) return;

        blockEntity.tickServer(level, pos, state);
    }

    private void tickServer(Level level, BlockPos pos, BlockState state) {
        boolean wasEnabled = this.isEnabled();
        boolean shouldSetChanged = false;

        if (this.isEnabled()) {
            this.fuelTime--;
            shouldSetChanged = true;
        }
        ItemStack inputStack = itemHandler.getStackInSlot(INPUT_SLOT);

        // Kein Input? Reset!
        if (inputStack.isEmpty()) {
            if (cookingProgress > 0) {
                cookingProgress = 0;
                shouldSetChanged = true;
            }
            if(shouldSetChanged) {
                setChanged();
            }
            return;
        }
        if (!ItemStack.isSameItemSameComponents(inputStack, lastInput)) {
            cookingProgress = 0;
            lastInput = inputStack.copy();
            shouldSetChanged = true;
        }

        SingleRecipeInput recipeInput = new SingleRecipeInput(inputStack);
        Optional<RecipeHolder<SmeltingRecipe>> recipeHolder = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, recipeInput, level);
        if (recipeHolder.isPresent()) {
            SmeltingRecipe recipe = recipeHolder.get().value();
            cookingTotalTime = recipe.getCookingTime();

            if (canCook(recipe, recipeInput)) {
                //Fuel needed?

                if (!this.isEnabled()) {
                    if (!tryConsumeFuel()) {
                        if (shouldSetChanged) {
                            setChanged();
                        }
                        return;
                    }
                    shouldSetChanged = true;
                }
                cookingProgress++;
                shouldSetChanged = true;

                // Fertig gekocht!
                if (cookingProgress >= cookingTotalTime) {
                    cook(recipe, recipeInput);
                    cookingProgress = 0;
                    lastInput = itemHandler.getStackInSlot(INPUT_SLOT).copy();
                    shouldSetChanged = true;
                }
            } else {
                // Kann nicht kochen (Output voll?) → Progress beibehalten aber nicht erhöhen
                // Optional: Progress langsam reduzieren
                if (cookingProgress > 0) {
                    cookingProgress = Math.max(0, cookingProgress - 2);
                    shouldSetChanged = true;
                }
            }
        } else {
            // Kein gültiges Rezept → Reset
            if (cookingProgress > 0) {
                cookingProgress = 0;
                shouldSetChanged = true;
            }
        }
        if(wasEnabled != this.isEnabled()) {
            shouldSetChanged = true;
            //#TODO change block state for visual change
        }

        if (shouldSetChanged) {
            setChanged();
        }
    }

    private boolean isEnabled() {
        return this.fuelTime > 0;
    }

    private boolean tryConsumeFuel() {
        ItemStack fuelStack = itemHandler.getStackInSlot(FUEL_SLOT);
        if (fuelStack.isEmpty()) {
            return false;
        }
        int burnTime = fuelStack.getBurnTime(null);
        if (burnTime <= 0) {
            return false;
        }

        this.fuelTime = burnTime;
        this.maxFuelTime = burnTime;

        ItemStack containerItem = fuelStack.getCraftingRemainingItem();
        itemHandler.extractItem(FUEL_SLOT, 1, false);

        if(!containerItem.isEmpty()) {
            if (itemHandler.getStackInSlot(FUEL_SLOT).isEmpty()) {
                itemHandler.setStackInSlot(FUEL_SLOT, containerItem);
            } else {
                //#TODO drop if no place for empty fuel container
            }
        }
        return true;
    }

    private static boolean isFuel(ItemStack stack) {
        return getBurnDuration(stack) > 0;
    }

    private static int getBurnDuration(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        return stack.getBurnTime(null);
    }

    private boolean canCook(SmeltingRecipe recipe, SingleRecipeInput input) {
        ItemStack inputStack = itemHandler.getStackInSlot(INPUT_SLOT);
        if (inputStack.isEmpty()) {
            return false;
        }

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

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.putInt("progress", cookingProgress);
        tag.putInt("total_time", cookingTotalTime);
        tag.putInt("fuel_time", fuelTime);
        tag.putInt("max_fuel_time", maxFuelTime);
        tag.put("last_input", lastInput.saveOptional(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        cookingProgress = tag.getInt("progress");
        cookingTotalTime = tag.getInt("total_time");
        fuelTime = tag.getInt("fuel_time");
        maxFuelTime = tag.getInt("max_fuel_time");
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
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new ToadToasterMenu(i, inventory, this, this.data);
    }

    public void drops(){
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }
}
