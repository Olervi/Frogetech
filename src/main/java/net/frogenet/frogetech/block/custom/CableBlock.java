package net.frogenet.frogetech.block.custom;

import com.mojang.serialization.MapCodec;
import net.frogenet.frogetech.block.entity.CableBlockEntity;
import net.frogenet.frogetech.energy.network.QuakNetworkManager;
import net.frogenet.frogetech.energy.network.QuakNetworkMember;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class CableBlock extends BaseEntityBlock {

    public static final MapCodec<CableBlock> CODEC = simpleCodec(CableBlock::new);

    //Each Dir one Property
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    //Core
    private static final VoxelShape CORE = Block.box(6, 6, 6, 10, 10, 10);

    //Arm in each dir
    private static final VoxelShape ARM_NORTH = Block.box(6, 6, 0, 10, 10, 6);
    private static final VoxelShape ARM_SOUTH = Block.box(6, 6, 10, 10, 10, 16);
    private static final VoxelShape ARM_EAST = Block.box(10, 6, 6, 16, 10, 10);
    private static final VoxelShape ARM_WEST = Block.box(0, 6, 6, 6, 10, 10);
    private static final VoxelShape ARM_UP = Block.box(6, 10, 6, 10, 16, 10);
    private static final VoxelShape ARM_DOWN = Block.box(6, 0, 6, 10, 6, 10);


    public CableBlock(Properties properties) {
        super(properties);
        //Default no connection
        registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    //Connection Logic

    private boolean canConnectTo(LevelAccessor level, BlockPos pos, Direction dir) {
        BlockPos neighborPos = pos.relative(dir);
        BlockState neighborState = level.getBlockState(neighborPos);

        // Other cable -> connect always
        if (neighborState.getBlock() instanceof CableBlock) return true;

        // Side-aware connection to network members
        BlockEntity be = level.getBlockEntity(neighborPos);
        if (be instanceof QuakNetworkMember member) {
            // From the neighbor perspective, the cable touches the opposite side.
            return member.canConnectFrom(dir.getOpposite());
        }
        return false;
    }

    private BlockState getStateForPos(LevelAccessor level, BlockPos pos) {
        return this.defaultBlockState()
                .setValue(NORTH, canConnectTo(level, pos, Direction.NORTH))
                .setValue(EAST, canConnectTo(level, pos, Direction.EAST))
                .setValue(SOUTH, canConnectTo(level, pos, Direction.SOUTH))
                .setValue(WEST, canConnectTo(level, pos, Direction.WEST))
                .setValue(UP, canConnectTo(level, pos, Direction.UP))
                .setValue(DOWN, canConnectTo(level, pos, Direction.DOWN));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context){
        return getStateForPos(context.getLevel(), context.getClickedPos());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(getPropertyForDirection(direction),
                canConnectTo(level, pos, direction));
    }

    private BooleanProperty getPropertyForDirection(Direction dir) {
        return switch (dir) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    //Network Events

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos,
                        BlockState oldState, boolean movedByPiston){
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if(!level.isClientSide){
            BlockEntity be = level.getBlockEntity(pos);
            if(be instanceof QuakNetworkMember member){
                QuakNetworkManager.get(level).onMemberAdded(member);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean movedByPiston){
        if(!level.isClientSide && state.getBlock() != newState.getBlock()) {
            QuakNetworkManager.get(level).onMemberRemoved(pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    //Rendering

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level,
                                  BlockPos pos, CollisionContext context) {
        VoxelShape shape = CORE;
        if(state.getValue(NORTH)) shape = Shapes.or(shape, ARM_NORTH);
        if(state.getValue(EAST))  shape = Shapes.or(shape, ARM_EAST);
        if(state.getValue(WEST))  shape = Shapes.or(shape, ARM_WEST);
        if(state.getValue(SOUTH)) shape = Shapes.or(shape, ARM_SOUTH);
        if(state.getValue(UP))    shape = Shapes.or(shape, ARM_UP);
        if(state.getValue(DOWN))  shape = Shapes.or(shape, ARM_DOWN);
        return shape;
    }

    //BE

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CableBlockEntity(pos, state);
    }

}
