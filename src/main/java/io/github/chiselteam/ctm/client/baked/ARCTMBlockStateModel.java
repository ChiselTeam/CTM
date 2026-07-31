package io.github.chiselteam.ctm.client.baked;

import io.github.chiselteam.ctm.api.model.CTMOverlayRule;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.CTMLogicAR;
import io.github.chiselteam.ctm.api.model.ConnectedTextureBlockModelPart;
import io.github.chiselteam.ctm.api.geometry.ARCTMKey;
import io.github.chiselteam.ctm.client.AbstractConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.impl.model.CTMPartBuilder;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ARCTMBlockStateModel extends AbstractConnectedTextureBlockStateModel<ARCTMKey> {

    protected final Map<Direction, BakedQuad[][]> connectedQuads;
    protected final Map<CTMOverlayRule, Map<Direction, BakedQuad>> ruleQuads;

    public ARCTMBlockStateModel(Set<Direction> connectedFaces, Set<Direction> unculledFaces, boolean renderOverlayOnAllFaces, Map<Direction, BakedQuad[]> baseQuads, Map<Direction, BakedQuad[][]> connectedQuads, TextureAtlasSprite particle, CTMVariant variant, CTMBlockPredicate connectionPredicate, List<CTMOverlayRule> overlayRules, Map<CTMOverlayRule, Map<Direction, BakedQuad>> ruleQuads) {
        super(connectedFaces, unculledFaces, renderOverlayOnAllFaces, baseQuads, particle, variant, connectionPredicate, overlayRules, computeTotalFlags(baseQuads, connectedQuads, ruleQuads));
        this.connectedQuads = connectedQuads;
        this.ruleQuads = ruleQuads;
    }

    private static int computeTotalFlags(Map<Direction, BakedQuad[]> baseQuads, Map<Direction, BakedQuad[][]> connectedQuads, Map<CTMOverlayRule, Map<Direction, BakedQuad>> ruleQuads) {
        int flags = 0;
        for (BakedQuad[] quads : baseQuads.values()) {
            for (BakedQuad quad : quads) if (quad != null) flags |= quad.materialInfo().flags();
        }
        for (BakedQuad[][] quads : connectedQuads.values()) {
            for (BakedQuad[] subQuads : quads) {
                for (BakedQuad quad : subQuads) if (quad != null) flags |= quad.materialInfo().flags();
            }
        }
        for (Map<Direction, BakedQuad> sideMap : ruleQuads.values()) {
            for (BakedQuad quad : sideMap.values()) if (quad != null) flags |= quad.materialInfo().flags();
        }
        return flags;
    }

    public ARCTMBlockStateModel(Set<Direction> connectedFaces, Set<Direction> unculledFaces, boolean renderOverlayOnAllFaces, Map<Direction, BakedQuad[]> baseQuads, Map<Direction, BakedQuad[][]> connectedQuads, TextureAtlasSprite particle, CTMVariant variant) {
        super(connectedFaces, unculledFaces, renderOverlayOnAllFaces, baseQuads, particle, variant);
        this.connectedQuads = connectedQuads;
        this.ruleQuads = Map.of();
    }

    @Override
    protected ARCTMKey computeCTMKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return ARCTMKey.of(CTMLogicAR.get(pos));
    }

    @Override
    protected ConnectedTextureBlockModelPart createPart(ARCTMKey key, long overlayMask) {
        return CTMPartBuilder.create(
                baseQuads,
                unculledFaces,
                particleMaterial,
                (side, faceQuads) -> {
                    appendConnectedQuads(key, side, faceQuads);
                    appendOverlayQuads(overlayMask, side, faceQuads);
                }
        );
    }

    protected void appendOverlayQuads(long mask, Direction side, List<BakedQuad> faceQuads) {
        if (mask == 0) return;
        int ruleCount = Math.min(overlayRules.size(), 10);
        for (int i = 0; i < ruleCount; i++) {
            if ((mask & (1L << (i * 6 + side.ordinal()))) != 0) {
                CTMOverlayRule rule = overlayRules.get(i);
                Map<Direction, BakedQuad> quads = ruleQuads.get(rule);
                if (quads != null) {
                    BakedQuad quad = quads.get(side);
                    if (quad != null) {
                        faceQuads.add(quad);
                    }
                }
            }
        }
    }

    private void appendConnectedQuads(ARCTMKey key, Direction side, List<BakedQuad> faceQuads) {
        BakedQuad[][] conn = connectedQuads.get(side);
        if (conn == null) {
            return;
        }

        int ar = key.ordinal();

        for (int i = 0; i < 4; i++) {
            BakedQuad[] cornerQuads = conn[i];
            if (cornerQuads == null) {
                continue;
            }

            BakedQuad quad = cornerQuads[ar];
            if (quad != null) {
                faceQuads.add(quad);
            }
        }
    }
}
