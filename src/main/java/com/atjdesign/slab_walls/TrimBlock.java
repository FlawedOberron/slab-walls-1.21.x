package com.atjdesign.slab_walls;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.StairShape;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class TrimBlock extends StairsBlock {
    public static final MapCodec<TrimBlock> CODEC = Block.createCodec(TrimBlock::new);

    @Override
    public MapCodec<TrimBlock> getCodec()
    {
        return  CODEC;
    }

    public TrimBlock()
    {
        super(Blocks.ACACIA_STAIRS.getDefaultState().with(FACING, Direction.NORTH).with(HALF, BlockHalf.BOTTOM).with(SHAPE, StairShape.STRAIGHT).with(WATERLOGGED, Boolean.valueOf(false)), AbstractBlock.Settings.create());
    }

    public TrimBlock(AbstractBlock.Settings settings)
    {
        super(Blocks.ACACIA_STAIRS.getDefaultState().with(FACING, Direction.NORTH).with(HALF, BlockHalf.BOTTOM).with(SHAPE, StairShape.STRAIGHT).with(WATERLOGGED, Boolean.valueOf(false)), settings);
    }

    private VoxelShape getFullTrimEdge (BlockHalf half, int rotation)
    {
        while (rotation < 0) {
            rotation += 4;
        }
        while (rotation > 3) {
            rotation -= 4;
        }

        float bot = 0f;
        float top = 0.5f;

        if (half == BlockHalf.TOP)
        {
            top = 1f;
            bot = 0.5f;
        }
        double startX;
        double startZ;
        double endX;
        double endZ;

        if (rotation == 1)
        {
            startX = 0.5f;
            startZ = 0f;
            endX = 1f;
            endZ = 1f;
        }
        else if (rotation == 2)
        {
            startX = 0f;
            startZ = 0.5f;
            endX = 1f;
            endZ = 1f;
        }
        else if (rotation == 3)
        {
            startX = 0f;
            startZ = 0f;
            endX = 0.5f;
            endZ = 1f;
        }
        else
        {
            startX = 0f;
            startZ = 0f;
            endX = 1f;
            endZ = 0.5f;
        }

        return VoxelShapes.cuboid(startX, bot, startZ, endX, top, endZ);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        // Get the facing...
        var facing = state.get(FACING);

        int rot = facing == Direction.NORTH ? 0 :
                facing == Direction.EAST ? 1 :
                    facing == Direction.SOUTH ? 2 : 3;


        // Get the shape...
        var shape = state.get(SHAPE);

        if (shape == StairShape.STRAIGHT) {
            return getFullTrimEdge(state.get(HALF), rot);
        }
        else if (shape == StairShape.INNER_LEFT) {
            return VoxelShapes.combine(getFullTrimEdge(state.get(HALF), rot), getFullTrimEdge(state.get(HALF), rot - 1), BooleanBiFunction.OR);
        }
        else if (shape == StairShape.INNER_RIGHT) {
            return VoxelShapes.combine(getFullTrimEdge(state.get(HALF), rot), getFullTrimEdge(state.get(HALF), rot + 1), BooleanBiFunction.OR);
        }
        else if (shape == StairShape.OUTER_LEFT) {
            return VoxelShapes.combine(getFullTrimEdge(state.get(HALF), rot), getFullTrimEdge(state.get(HALF), rot - 1), BooleanBiFunction.AND);
        }
        else if (shape == StairShape.OUTER_RIGHT) {
            return VoxelShapes.combine(getFullTrimEdge(state.get(HALF), rot), getFullTrimEdge(state.get(HALF), rot + 1), BooleanBiFunction.AND);
        }

        return VoxelShapes.cuboid(.25f, 0f, .25f, .75f, .5f, .75f);
    }
}
