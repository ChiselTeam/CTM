package io.github.chiselteam.ctm.api.texture;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;

import java.util.List;

@FunctionalInterface
public interface FaceQuadAppender {
    void append(Direction face, List<BakedQuad> faceQuads);
}
