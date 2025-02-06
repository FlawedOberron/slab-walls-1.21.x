package com.atjdesign.slab_walls;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.WallBlock;
import net.minecraft.block.enums.WallShape;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class SlimWallBlock extends WallBlock {
    public SlimWallBlock () {
        super(AbstractBlock.Settings.create());
    }

    public SlimWallBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        var bit = 1f / 16f;
        var shape = VoxelShapes.cuboid(bit * 7f, 0f, bit * 7f, bit * 9f, 1f, bit * 9f);

        var trimTop = (state.get(NORTH_SHAPE) != WallShape.NONE && state.get(EAST_SHAPE) != WallShape.NONE && state.get(SOUTH_SHAPE) != WallShape.NONE && state.get(WEST_SHAPE) != WallShape.NONE);

        if (trimTop)
        {
            shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0f, 0f, 0f, 1f, bit * 14f, 1f), BooleanBiFunction.AND);
        }

        if (state.get(NORTH_SHAPE) != WallShape.NONE)
        {
            shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(bit * 7f, 0f, 0f, bit * 9f, state.get(NORTH_SHAPE) == WallShape.LOW ? (bit * 14f) : 1f, .5f), BooleanBiFunction.OR);
        }

        if (state.get(EAST_SHAPE) != WallShape.NONE)
        {
            shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(.5f, 0f, bit * 7f, 1f, state.get(EAST_SHAPE) == WallShape.LOW ? (bit * 14f) : 1f, bit * 9f), BooleanBiFunction.OR);
        }

        if (state.get(SOUTH_SHAPE) != WallShape.NONE)
        {
            shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(bit * 7f, 0f, .5f, bit * 9f, state.get(SOUTH_SHAPE) == WallShape.LOW ? (bit * 14f) : 1f, 1f), BooleanBiFunction.OR);
        }

        if (state.get(WEST_SHAPE) != WallShape.NONE)
        {
            shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0f, 0f, bit * 7f, .5f, state.get(WEST_SHAPE) == WallShape.LOW ? (bit * 14f) : .5f, bit * 9f), BooleanBiFunction.OR);
        }

        return shape;
    }
}
