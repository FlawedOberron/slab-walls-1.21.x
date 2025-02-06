package com.atjdesign.slab_walls;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.booleans.BooleanPredicate;
import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.block.enums.StairShape;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PillarBlock extends Block implements net.minecraft.block.Waterloggable {

//    public static final Block BASE_PILLAR_BLOCK = ModItems.register(new net.minecraft.block.PillarBlock(AbstractBlock.Settings.copy(Blocks.STONE)),
//            RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(SlabWalls.MOD_ID, "base_pillar_block")),
//            false);

    public static final MapCodec<PillarBlock> CODEC = Block.createCodec(PillarBlock::new);
    public static final BooleanProperty UP = BooleanProperty.of("up");
    public static final BooleanProperty DOWN = BooleanProperty.of("down");
    public static final BooleanProperty TOP = BooleanProperty.of("top");
    public static final BooleanProperty BOTTOM = BooleanProperty.of("bottom");
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    @Override
    public MapCodec<PillarBlock> getCodec()
    {
        return CODEC;
    }

    public PillarBlock()
    {
        super(AbstractBlock.Settings.create());
        setDefaultState(getDefaultState().with(UP, false).with(DOWN, false).with(TOP, false).with(BOTTOM, false).with(WATERLOGGED, false));
    }

    public PillarBlock(Settings settings)
    {
        super(settings);
        setDefaultState(getDefaultState().with(UP, false).with(DOWN, false).with(TOP, false).with(BOTTOM, false).with(WATERLOGGED, false));
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN, TOP, BOTTOM, WATERLOGGED);

        super.appendProperties(builder);
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        UpdateBlockStateAtPos(state, world, pos);
        //super.neighborUpdate(state, world, pos, sourceBlock, wireOrientation, notify);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        UpdateBlockStateAtPos(state, world, pos);
        //super.onPlaced(world, pos, state, placer, itemStack);
    }

    @Override
    protected boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState()
                .with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).isOf(Fluids.WATER));
    }

    private BlockState UpdateBlockStateAtPos(BlockState state, World world, BlockPos pos)
    {
//        state.with(UP, false);
//        state.with(TOP, false);
//        state.with(DOWN, false);
//        state.with(BOTTOM, false);
//
//        state.with(Properties.WATERLOGGED, world.getFluidState(pos).isOf(Fluids.WATER));

        var up = pos.up();
        var down = pos.down();

        var uB = world.getBlockEntity(up);
        var dB = world.getBlockEntity(down);
        var uBS = world.getBlockState(up);// world.getBlockEntity(pos.up());
        var dBS = world.getBlockState(down);

        var log = Logger.getLogger("Pillar");

        // Air Tile...
        if (world.isAir(up))
        {
            state = state.with(UP, false);
            state = state.with(TOP, false);
        }
        // Full Tile...
        else if (uBS.isFullCube(world, up) || uBS.isSideSolidFullSquare(world, up, Direction.DOWN))
        {
            state = state.with(UP, false);
            state = state.with(TOP, true);
        }
        else
        {
            var bType = uBS.getBlock().getClass().getSimpleName();
            var name = uBS.getBlock().getTranslationKey();

            if (bType.equals("PillarBlock")) {
                state = state.with(UP, false);
                state = state.with(TOP, false);
            }
            else if (bType.equals("StairsBlock") || name.endsWith("_stairs"))
            {
                var bHalf = uBS.get(StairsBlock.HALF);

                state = state.with(UP, bHalf == BlockHalf.TOP);
                state = state.with(TOP, true);
            }
            else if (bType.equals("TrimBlock"))
            {
                var bHalf = uBS.get(StairsBlock.HALF);
                var dup = pos.up(2);
                var dupS = world.getBlockState(dup);

                var shouldUp = bHalf == BlockHalf.TOP &&
                        // Get the next block up, make sure it's solid...
                        (dupS.isFullCube(world, dup) || dupS.isSideSolidFullSquare(world, dup, Direction.DOWN));

                state = state.with(UP, shouldUp);
                state = state.with(TOP, true);

            }
            else if (bType.equals("SlabBlock") || name.endsWith("_slab"))
            {
                var sType = uBS.get(Properties.SLAB_TYPE);

                state = state.with(TOP, true);
                state = state.with(UP, sType == SlabType.TOP);
            }
            else
            {
                state = state.with(UP, false);
                state = state.with(TOP, true);
            }
        }

        if (world.isAir(down))
        {
            state = state.with(DOWN, false);
            state = state.with(BOTTOM, false);
        }
        else if (dBS.isFullCube(world, down) || dBS.isSideSolidFullSquare(world, down, Direction.UP))
        {
            state = state.with(DOWN, false);
            state = state.with(BOTTOM, true);
        }
        else
        {
            var bType = dBS.getBlock().getClass().getSimpleName();
            var name = dBS.getBlock().getTranslationKey();

            if (bType.equals("PillarBlock")) {
                state = state.with(DOWN, false);
                state = state.with(BOTTOM, false);
            }
            else if (bType.equals("StairsBlock") || name.endsWith("_stairs"))
            {
                var bHalf = dBS.get(StairsBlock.HALF);

                state = state.with(DOWN, bHalf == BlockHalf.BOTTOM);
                state = state.with(BOTTOM, true);
            }
            else if (bType.equals("TrimBlock"))
            {
                var bHalf = dBS.get(StairsBlock.HALF);
                var ddp = pos.down(2);
                var ddpS = world.getBlockState(ddp);

                var shouldDown = bHalf == BlockHalf.BOTTOM &&
                        // Get the next block up, make sure it's solid...
                        (ddpS.isFullCube(world, ddp) || ddpS.isSideSolidFullSquare(world, ddp, Direction.UP));

                state = state.with(DOWN, shouldDown);
                state = state.with(BOTTOM, true);

            }
            else if (bType.equals("SlabBlock") || name.endsWith("_slab"))
            {
                var sType = dBS.get(Properties.SLAB_TYPE);

                state = state.with(BOTTOM, true);
                state = state.with(DOWN, sType == SlabType.BOTTOM);
            }
            else
            {
                state = state.with(DOWN, false);
                state = state.with(BOTTOM, true);
            }
        }

        world.setBlockState(pos, state);
        return state;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
    {
        VoxelShape shape = VoxelShapes.cuboid((1f / 16f) * 4f, 0f, (1f / 16f) * 4f, (1f / 16f) * 12f, 1f, (1f / 16f) * 12f);

        return shape;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape shape = VoxelShapes.cuboid((1f / 16f) * 4f, 0f, (1f / 16f) * 4f, (1f / 16f) * 12f, 1f, (1f / 16f) * 12f);

        if (state.get(DOWN))
        {
            shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0f, -1f, 0f, 1f, 0f, 1f), BooleanBiFunction.OR);
        }

        if (state.get(UP))
        {
            shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0f, 1f, 0f, 1f, 2f, 1f), BooleanBiFunction.OR);
        }

        return  shape;
    }
}
