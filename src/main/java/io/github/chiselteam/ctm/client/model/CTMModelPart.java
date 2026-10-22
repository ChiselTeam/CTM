package io.github.chiselteam.ctm.client.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;

import java.util.List;

public interface CTMModelPart {
    List<BakedQuad> getQuads(Direction side);

    BakedTexture particleMaterial();

    default boolean useAmbientOcclusion() {
        return true;
    }
}
