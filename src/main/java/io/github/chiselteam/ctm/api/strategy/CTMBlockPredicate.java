package io.github.chiselteam.ctm.api.strategy;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

@FunctionalInterface
public interface CTMBlockPredicate {
    boolean test(Block target, BlockState neighbor);

    static CTMBlockPredicate sameBlock() {
        return (target, neighbor) -> neighbor.is(target);
    }

    static CTMBlockPredicate block(Block block) {
        return new BlockMatchPredicate(block);
    }

    static CTMBlockPredicate state(ResolvedBlockStateMatcher matcher) {
        return new BlockStateMatchPredicate(matcher);
    }

    static CTMBlockPredicate any(CTMBlockPredicate... predicates) {
        return new AnyOfCTMBlockPredicate(List.of(predicates));
    }

    static CTMBlockPredicate all(CTMBlockPredicate... predicates) {
        return new AllOfCTMBlockPredicate(List.of(predicates));
    }

    record BlockMatchPredicate(Block block) implements CTMBlockPredicate {
        @Override
        public boolean test(Block target, BlockState neighbor) {
            return neighbor.is(block);
        }
    }

    record BlockStateMatchPredicate(ResolvedBlockStateMatcher matcher) implements CTMBlockPredicate {
        @Override
        public boolean test(Block target, BlockState neighbor) {
            return matcher.test(neighbor);
        }
    }

    record AnyOfCTMBlockPredicate(List<CTMBlockPredicate> predicates) implements CTMBlockPredicate {
        @Override
        public boolean test(Block target, BlockState neighbor) {
            for (CTMBlockPredicate predicate : predicates) {
                if (predicate.test(target, neighbor)) {
                    return true;
                }
            }
            return false;
        }
    }

    record AllOfCTMBlockPredicate(List<CTMBlockPredicate> predicates) implements CTMBlockPredicate {
        @Override
        public boolean test(Block target, BlockState neighbor) {
            for (CTMBlockPredicate predicate : predicates) {
                if (!predicate.test(target, neighbor)) {
                    return false;
                }
            }
            return true;
        }
    }
}
