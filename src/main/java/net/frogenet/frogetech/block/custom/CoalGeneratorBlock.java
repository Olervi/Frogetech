package net.frogenet.frogetech.block.custom;

import com.mojang.serialization.MapCodec;
import net.frogenet.frogetech.block.entity.CoalGeneratorBlockEntity;
import net.frogenet.frogetech.block.entity.ModBlockEntities;
import net.frogenet.frogetech.energy.network.QuakNetworkManager;
import net.frogenet.frogetech.energy.network.QuakNetworkMember;
import net.frogenet.frogetech.entity.custom.FrogEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CoalGeneratorBlock extends BaseEntityBlock {
    public static final MapCodec<CoalGeneratorBlock> CODEC = simpleCodec(CoalGeneratorBlock::new);

    public CoalGeneratorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CoalGeneratorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null :
                createTickerHelper(type, ModBlockEntities.COAL_GENERATOR_BE.get(),
                        CoalGeneratorBlockEntity::tick);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level,
                                            BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CoalGeneratorBlockEntity generator) {
                // Attach a leashed frog (mod or vanilla) if the player has one nearby
                List<Mob> leashedFrogs = level.getEntitiesOfClass(
                        Mob.class,
                        new AABB(pos).inflate(10),
                        mob -> (mob instanceof FrogEntity || mob instanceof Frog)
                                && mob.isLeashed() && mob.getLeashHolder() == player
                );
                if (!leashedFrogs.isEmpty()) {
                    Mob mob = leashedFrogs.get(0);
                    mob.dropLeash(true, true);
                    mob.setNoAi(true);
                    float lockedYRot = player.getYRot() + 180f;
                    if (mob instanceof FrogEntity modFrog) {
                        modFrog.setSittingPos(pos);
                    } else {
                        CompoundTag data = mob.getPersistentData();
                        data.putInt("FrogSittingX", pos.getX());
                        data.putInt("FrogSittingY", pos.getY());
                        data.putInt("FrogSittingZ", pos.getZ());
                    }
                    mob.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, lockedYRot, 0f);
                    generator.setSittingFrog(mob, lockedYRot);
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }

                player.openMenu(generator, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos,
                        BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof QuakNetworkMember member) {
                QuakNetworkManager.get(level).onMemberAdded(member);
            }
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide() && state.getBlock() != newState.getBlock()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CoalGeneratorBlockEntity generator) {
                generator.releaseSittingFrog(level);
                generator.drops();
            }
            QuakNetworkManager.get(level).onMemberRemoved(pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
