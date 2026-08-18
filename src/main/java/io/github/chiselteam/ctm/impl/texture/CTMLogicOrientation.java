package io.github.chiselteam.ctm.impl.texture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

public record CTMLogicOrientation(Direction.Axis axis) {
    public static CTMLogicOrientation of(BlockState state) {
        Direction.Axis axis = state.hasProperty(RotatedPillarBlock.AXIS)
                ? state.getValue(RotatedPillarBlock.AXIS)
                : Direction.Axis.Y;
        return new CTMLogicOrientation(axis);
    }

    public BlockPos toLocal(BlockPos pos) {
        return switch (axis) {
            case X -> new BlockPos(-pos.getY(), pos.getX(), pos.getZ());
            case Y -> pos;
            case Z -> new BlockPos(pos.getX(), pos.getZ(), -pos.getY());
        };
    }

    public Direction toLocal(Direction direction) {
        return switch (axis) {
            case X -> direction(-direction.getStepY(), direction.getStepX(), direction.getStepZ());
            case Y -> direction;
            case Z -> direction(direction.getStepX(), direction.getStepZ(), -direction.getStepY());
        };
    }

    public Direction toWorld(Direction direction) {
        return switch (axis) {
            case X -> direction(direction.getStepY(), -direction.getStepX(), direction.getStepZ());
            case Y -> direction;
            case Z -> direction(direction.getStepX(), -direction.getStepZ(), direction.getStepY());
        };
    }

    private static Direction direction(int x, int y, int z) {
        for (Direction direction : Direction.values()) {
            if (direction.getStepX() == x && direction.getStepY() == y && direction.getStepZ() == z) {
                return direction;
            }
        }
        throw new IllegalArgumentException("Not a cardinal direction: " + x + ", " + y + ", " + z);
    }
}
