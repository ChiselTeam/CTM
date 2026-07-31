package io.github.chiselteam.ctm.api.model;

import io.github.chiselteam.ctm.api.strategy.ResolvedBlockStateMatcher;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class CTMOverlayConditions {

    public record SelfStateCondition(ResolvedBlockStateMatcher matcher) implements CTMOverlayCondition {
        @Override
        public boolean test(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction face) {
            return matcher.test(state);
        }
    }

    public record NeighborCondition(Direction direction, ResolvedBlockStateMatcher matcher) implements CTMOverlayCondition {
        @Override
        public boolean test(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction face) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            BlockState neighborAppearance = neighborState.getAppearance(level, neighborPos, direction.getOpposite(), state, pos);
            return matcher.test(neighborAppearance);
        }
    }

    /**
     * Matches when <em>any</em> of the six neighboring blocks around the target position
     * satisfies the provided matcher. This is useful for concise JSON where a single
     * neighbor condition should apply to all faces without listing each direction.
     */
    public record AnyNeighborCondition(ResolvedBlockStateMatcher matcher) implements CTMOverlayCondition {
        @Override
        public boolean test(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction face) {
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pos.relative(dir);
                BlockState neighborState = level.getBlockState(neighborPos);
                BlockState neighborAppearance = neighborState.getAppearance(level, neighborPos, dir.getOpposite(), state, pos);
                if (matcher.test(neighborAppearance)) {
                    return true;
                }
            }
            return false;
        }
    }

    public record AllOfOverlayCondition(List<CTMOverlayCondition> conditions) implements CTMOverlayCondition {
        @Override
        public boolean test(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction face) {
            for (CTMOverlayCondition condition : conditions) {
                if (!condition.test(level, pos, state, face)) {
                    return false;
                }
            }
            return true;
        }
    }

    public record AnyOfOverlayCondition(List<CTMOverlayCondition> conditions) implements CTMOverlayCondition {
        @Override
        public boolean test(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction face) {
            for (CTMOverlayCondition condition : conditions) {
                if (condition.test(level, pos, state, face)) {
                    return true;
                }
            }
            return false;
        }
    }
}
