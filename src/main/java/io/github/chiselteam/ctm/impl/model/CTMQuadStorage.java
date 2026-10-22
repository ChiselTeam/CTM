package io.github.chiselteam.ctm.impl.model;

import io.github.chiselteam.ctm.client.model.CTMBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;

import java.util.function.Consumer;

public interface CTMQuadStorage {
    default void visitQuadArrays(Consumer<BakedQuad[]> visitor) {}

    default void visitChildModels(Consumer<CTMBakedModel> visitor) {}
}
