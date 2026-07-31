package io.github.chiselteam.ctm.api.model;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public record CTMOverlayRule(
        Material material,
        Set<Direction> faces,
        CTMOverlayCondition condition,
        int priority,
        int tintIndex,
        int emissivity
) {
    public boolean test(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction face) {
        if (faces != null && !faces.isEmpty() && !faces.contains(face)) {
            return false;
        }
        return condition.test(level, pos, state, face);
    }
}
