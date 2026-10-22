package io.github.chiselteam.ctm.api.model;

import io.github.chiselteam.ctm.client.model.BakedTexture;
import io.github.chiselteam.ctm.client.model.CTMModelPart;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record ConnectedTextureBlockModelPart(
        Map<Direction, List<BakedQuad>> quads,
        List<BakedQuad> unculledQuads,
        BakedTexture particleMaterial,
        boolean ambientOcclusionEnabled
) implements CTMModelPart {

    public ConnectedTextureBlockModelPart(Map<Direction, List<BakedQuad>> quads, List<BakedQuad> unculledQuads, BakedTexture particleMaterial) {
        this(quads, unculledQuads, particleMaterial, true);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable Direction side) {
        return side == null ? unculledQuads : quads.get(side);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return ambientOcclusionEnabled;
    }
}
