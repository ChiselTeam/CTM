package io.github.chiselteam.ctm.api.strategy;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface CTMBlockPredicate {
    boolean test(Block target, BlockState neighbor);

    static CTMBlockPredicate sameBlock() {
        return (target, neighbor) -> neighbor.is(target);
    }
}
