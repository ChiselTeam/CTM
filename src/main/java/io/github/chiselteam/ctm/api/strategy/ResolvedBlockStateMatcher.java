package io.github.chiselteam.ctm.api.strategy;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A matcher that checks if a {@link BlockState} matches a specific block and set of properties.
 * This should be resolved during model loading and not perform any registry lookups during rendering.
 */
public record ResolvedBlockStateMatcher(Block block, Map<Property<?>, Comparable<?>> properties) {
    public boolean test(BlockState state) {
        if (!state.is(block)) {
            return false;
        }
        for (Map.Entry<Property<?>, Comparable<?>> entry : properties.entrySet()) {
            if (!Objects.equals(state.getValue(entry.getKey()), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    public static Builder forBlock(Block block) {
        return new Builder(block);
    }

    public static class Builder {
        private final Block block;
        private final Map<Property<?>, Comparable<?>> properties = new HashMap<>();

        private Builder(Block block) {
            this.block = block;
        }

        public <T extends Comparable<T>> Builder with(Property<T> property, T value) {
            properties.put(property, value);
            return this;
        }

        public ResolvedBlockStateMatcher build() {
            return new ResolvedBlockStateMatcher(block, Map.copyOf(properties));
        }
    }
}
