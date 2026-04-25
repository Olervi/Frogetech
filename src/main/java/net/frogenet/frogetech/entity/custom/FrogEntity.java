package net.frogenet.frogetech.entity.custom;

import net.frogenet.frogetech.block.entity.AbstractQuakMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FrogEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final String CONTROLLER_NAME = "controller";

    @Nullable
    private BlockPos sittingPos = null;

    public FrogEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public boolean isSittingOnBlock() {
        return sittingPos != null;
    }

    @Nullable
    public BlockPos getSittingPos() {
        return sittingPos;
    }

    public void setSittingPos(BlockPos pos) {
        this.sittingPos = pos;
        this.setNoAi(true);
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
    }

    public void clearSittingPos() {
        this.sittingPos = null;
        this.setNoAi(false);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown() && isSittingOnBlock()) {
            if (!level().isClientSide()) {
                BlockEntity be = level().getBlockEntity(sittingPos);
                if (be instanceof AbstractQuakMachineBlockEntity machine) {
                    machine.releaseSittingFrog(level());
                } else {
                    clearSittingPos();
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (sittingPos != null) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", sittingPos.getX());
            posTag.putInt("y", sittingPos.getY());
            posTag.putInt("z", sittingPos.getZ());
            tag.put("SittingPos", posTag);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SittingPos")) {
            CompoundTag posTag = tag.getCompound("SittingPos");
            this.sittingPos = new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
            this.setNoAi(true);
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this , 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER_NAME, 5, this::predicate));
    }

    private PlayState predicate(AnimationState<FrogEntity> state) {
        //Move Anim
        if(state.isMoving()) {
            state.getController().setAnimation(RawAnimation.begin()
                    .thenLoop("animation.frog.walk"));
            return PlayState.CONTINUE;
        }

        //Idle Anim
        state.getController().setAnimation(RawAnimation.begin()
                .thenLoop("animation.frog.idle"));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache(){
        return cache;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.15D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

}
