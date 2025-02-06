package com.atjdesign.slab_walls;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
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
import org.joml.Vector3d;

import java.util.logging.Level;
import java.util.logging.Logger;

public class StrutBlock extends Block implements net.minecraft.block.Waterloggable {
    MapCodec<StrutBlock> CODEC = TrimBlock.createCodec(StrutBlock::new);
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final EnumProperty<BlockHalf> HALF = Properties.BLOCK_HALF;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final BooleanProperty FENCED = BooleanProperty.of("fenced");

    public  MapCodec<StrutBlock> getCodec()
    {
        return CODEC;
    }

    public StrutBlock()
    {
        super(AbstractBlock.Settings.create());
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(HALF, BlockHalf.BOTTOM).with(WATERLOGGED, false).with(FENCED, false));
    }

    public StrutBlock(Settings settings)
    {
        super(settings);
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(HALF, BlockHalf.BOTTOM).with(WATERLOGGED, false).with(FENCED, false));
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, WATERLOGGED, FENCED);

        super.appendProperties(builder);
    }

    @Override
    protected boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        boolean drop = true;

        if (state.get(HALF) == BlockHalf.TOP && world.getBlockState(pos.up()).isSideSolid(world, pos.up(), Direction.DOWN, SideShapeType.RIGID))
        {
            drop = false;
        }
        else if (state.get(HALF) == BlockHalf.BOTTOM && world.getBlockState(pos.down()).isSideSolid(world, pos.down(), Direction.UP, SideShapeType.RIGID))
        {
            drop = false;
        }
        else
        {
            var dir = state.get(FACING);
            var checkPos = dir == Direction.NORTH ? pos.south() :
                    dir == Direction.SOUTH ? pos.north() :
                            dir == Direction.EAST ? pos.west() : pos.east();

            if (world.getBlockState(checkPos).isSideSolid(world, checkPos, dir.getOpposite(), SideShapeType.RIGID)
                    || world.getBlockState(checkPos).getBlock().getClass().getSimpleName().equals("WallBlock")
                    || world.getBlockState(checkPos).getBlock().getClass().getSimpleName().equals("SlimWallBlock"))
            {
                drop = false;
            }
        }

        if (drop)
        {
            return  false;
        }

        return super.canPlaceAt(state, world, pos);
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        // Check for drops...
        boolean drop = true;

        if (state.get(HALF) == BlockHalf.TOP && world.getBlockState(pos.up()).isSideSolid(world, pos.up(), Direction.DOWN, SideShapeType.RIGID))
        {
            drop = false;
        }
        else if (state.get(HALF) == BlockHalf.BOTTOM && world.getBlockState(pos.down()).isSideSolid(world, pos.down(), Direction.UP, SideShapeType.RIGID))
        {
            drop = false;
        }
        else
        {
            var dir = state.get(FACING);
            var checkPos = dir == Direction.NORTH ? pos.south() :
                            dir == Direction.SOUTH ? pos.north() :
                            dir == Direction.EAST ? pos.west() : pos.east();

            if (world.getBlockState(checkPos).isSideSolid(world, checkPos, dir.getOpposite(), SideShapeType.RIGID)
                    || world.getBlockState(checkPos).getBlock().getClass().getSimpleName().equals("WallBlock")
                    || world.getBlockState(checkPos).getBlock().getClass().getSimpleName().equals("SlimWallBlock"))
            {
                drop = false;

                if (world.getBlockState(checkPos).getBlock().getClass().getSimpleName().equals("WallBlock")
                        || world.getBlockState(checkPos).getBlock().getClass().getSimpleName().equals("SlimWallBlock"))
                {
                    state.with(FENCED, true);
                }
            }
        }

        if (drop)
        {
            world.breakBlock(pos, true);
        }

        super.neighborUpdate(state, world, pos, sourceBlock, wireOrientation, notify);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        var hPos = ctx.getHitPos();
        var bPos = ctx.getBlockPos();
        var side = ctx.getSide();

        var half = (hPos.y - bPos.getY()) > 0.5 ? BlockHalf.TOP : BlockHalf.BOTTOM;

        // Calculate the Horizontal Facing...
        var hDir = side == Direction.UP || side == Direction.DOWN ?
                        ctx.getHorizontalPlayerFacing().getOpposite() :
                        side;

        var adjBP = hDir == Direction.NORTH ? ctx.getBlockPos().south() :
                    hDir == Direction.SOUTH ? ctx.getBlockPos().north() :
                    hDir == Direction.EAST ? ctx.getBlockPos().west() :
                            ctx.getBlockPos().east();

        var isFenced = ctx.getWorld().getBlockState(adjBP).getBlock().getClass().getSimpleName().equals("WallBlock") ||
                ctx.getWorld().getBlockState(adjBP).getBlock().getClass().getSimpleName().equals("SlimWallBlock");

        return this.getDefaultState()
                .with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).isOf(Fluids.WATER))
                .with(FACING, hDir)
                .with(HALF, half)
                .with(FENCED, isFenced);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        var pivot = new Vector3d(.5, .5, .5);
        var zRot = state.get(HALF) == BlockHalf.TOP ? 2 : 0;
        var yRot = state.get(FACING) == Direction.NORTH ? 0 :
                    state.get(FACING) == Direction.EAST ? 1 :
                    state.get(FACING) == Direction.SOUTH ? 2 : 3;

        var bit = 1.0 / 16.0;

        var boxOne = VoxelShapes.cuboid(ModItems.RotateBox(Direction.Axis.Y, ModItems.RotateBox(Direction.Axis.Z, new Box(bit * 4, 0, bit * 14, bit * 12, bit * 12, 1), zRot, pivot), yRot, pivot));
        var boxTwo = VoxelShapes.cuboid(ModItems.RotateBox(Direction.Axis.Y, ModItems.RotateBox(Direction.Axis.Z, new Box(bit * 4, 0, bit * 4, bit * 12, bit * 2, 1), zRot, pivot), yRot, pivot));
        var boxThree = VoxelShapes.cuboid(ModItems.RotateBox(Direction.Axis.Y, ModItems.RotateBox(Direction.Axis.Z, new Box(bit * 4, 0, 0.5, bit * 12, 0.5, 1), zRot, pivot), yRot, pivot));

        return VoxelShapes.combine(boxThree, VoxelShapes.combine(boxOne, boxTwo, BooleanBiFunction.OR), BooleanBiFunction.OR);
    }
}
