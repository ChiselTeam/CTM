package io.github.chiselteam.ctm.impl.model;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;

import java.util.function.Consumer;

public interface CTMQuadStorage {
    default void visitQuadArrays(Consumer<BakedQuad[]> visitor) {}

    default void visitChildModels(Consumer<BlockStateModel> visitor) {}
}
