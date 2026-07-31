package io.github.chiselteam.ctm.api.model;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface CTMOverlayCondition {
    boolean test(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction face);
}
