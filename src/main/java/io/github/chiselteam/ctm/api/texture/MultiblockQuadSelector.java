package io.github.chiselteam.ctm.api.texture;

import io.github.chiselteam.ctm.api.geometry.MultiblockCTMKey;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;

import java.util.List;

@FunctionalInterface
public interface MultiblockQuadSelector {
    void append(MultiblockCTMKey key, Direction side, List<BakedQuad> faceQuads);
}
