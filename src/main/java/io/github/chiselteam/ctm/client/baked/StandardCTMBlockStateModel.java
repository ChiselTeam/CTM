package io.github.chiselteam.ctm.client.baked;

import io.github.chiselteam.ctm.api.model.CTMOverlayRule;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.api.model.ConnectedTextureBlockModelPart;
import io.github.chiselteam.ctm.api.strategy.CTMLogic;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.geometry.StandardCTMKey;
import io.github.chiselteam.ctm.client.AbstractConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.impl.model.CTMPartBuilder;
import io.github.chiselteam.ctm.impl.texture.CTMFaceOrientation;
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

public class StandardCTMBlockStateModel extends AbstractConnectedTextureBlockStateModel<StandardCTMKey> {

    protected final Map<Direction, BakedQuad[][]> connectedQuads;
    protected final Map<CTMOverlayRule, Map<Direction, BakedQuad>> ruleQuads;
    protected final CTMFaceOrientation orientation;

    public StandardCTMBlockStateModel(Set<Direction> connectedFaces, Set<Direction> unculledFaces, boolean renderOverlayOnAllFaces, Map<Direction, BakedQuad[]> baseQuads, Map<Direction, BakedQuad[][]> connectedQuads, TextureAtlasSprite particle, CTMVariant variant, CTMBlockPredicate connectionPredicate, List<CTMOverlayRule> overlayRules, Map<CTMOverlayRule, Map<Direction, BakedQuad>> ruleQuads, boolean ambientOcclusion) {
        this(connectedFaces, unculledFaces, renderOverlayOnAllFaces, baseQuads, connectedQuads, particle, variant, connectionPredicate, overlayRules, ruleQuads, ambientOcclusion, CTMFaceOrientation.IDENTITY);
    }

    public StandardCTMBlockStateModel(Set<Direction> connectedFaces, Set<Direction> unculledFaces, boolean renderOverlayOnAllFaces, Map<Direction, BakedQuad[]> baseQuads, Map<Direction, BakedQuad[][]> connectedQuads, TextureAtlasSprite particle, CTMVariant variant, CTMBlockPredicate connectionPredicate, List<CTMOverlayRule> overlayRules, Map<CTMOverlayRule, Map<Direction, BakedQuad>> ruleQuads, boolean ambientOcclusion, CTMFaceOrientation orientation) {
        super(connectedFaces, unculledFaces, renderOverlayOnAllFaces, baseQuads, particle, variant, connectionPredicate, overlayRules, computeTotalFlags(baseQuads, connectedQuads, ruleQuads), ambientOcclusion);
        this.connectedQuads = connectedQuads;
        this.ruleQuads = ruleQuads;
        this.orientation = orientation;
    }

    public StandardCTMBlockStateModel(Set<Direction> connectedFaces, Set<Direction> unculledFaces, boolean renderOverlayOnAllFaces, Map<Direction, BakedQuad[]> baseQuads, Map<Direction, BakedQuad[][]> connectedQuads, TextureAtlasSprite particle, CTMVariant variant, CTMBlockPredicate connectionPredicate, List<CTMOverlayRule> overlayRules, Map<CTMOverlayRule, Map<Direction, BakedQuad>> ruleQuads) {
        super(connectedFaces, unculledFaces, renderOverlayOnAllFaces, baseQuads, particle, variant, connectionPredicate, overlayRules, computeTotalFlags(baseQuads, connectedQuads, ruleQuads));
        this.connectedQuads = connectedQuads;
        this.ruleQuads = ruleQuads;
        this.orientation = CTMFaceOrientation.IDENTITY;
    }

    private static int computeTotalFlags(Map<Direction, BakedQuad[]> baseQuads, Map<Direction, BakedQuad[][]> connectedQuads, Map<CTMOverlayRule, Map<Direction, BakedQuad>> ruleQuads) {
        int flags = 0;
        for (BakedQuad[] quads : baseQuads.values()) {
            for (BakedQuad quad : quads) {
                if (quad != null) flags |= quad.materialInfo().flags();
            }
        }
        for (BakedQuad[][] quads : connectedQuads.values()) {
            for (BakedQuad[] subQuads : quads) {
                for (BakedQuad quad : subQuads) {
                    if (quad != null) flags |= quad.materialInfo().flags();
                }
            }
        }
        for (Map<Direction, BakedQuad> sideMap : ruleQuads.values()) {
            for (BakedQuad quad : sideMap.values()) {
                if (quad != null) flags |= quad.materialInfo().flags();
            }
        }
        return flags;
    }

    public StandardCTMBlockStateModel(Set<Direction> connectedFaces, Set<Direction> unculledFaces, boolean renderOverlayOnAllFaces, Map<Direction, BakedQuad[]> baseQuads, Map<Direction, BakedQuad[][]> connectedQuads, TextureAtlasSprite particle, CTMVariant variant) {
        super(connectedFaces, unculledFaces, renderOverlayOnAllFaces, baseQuads, particle, variant);
        this.connectedQuads = connectedQuads;
        this.ruleQuads = Map.of();
        this.orientation = CTMFaceOrientation.IDENTITY;
    }

    @Override
    protected StandardCTMKey computeCTMKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return new StandardCTMKey(
                computeFace(level, pos, state, Direction.DOWN, orientation),
                computeFace(level, pos, state, Direction.UP, orientation),
                computeFace(level, pos, state, Direction.NORTH, orientation),
                computeFace(level, pos, state, Direction.SOUTH, orientation),
                computeFace(level, pos, state, Direction.WEST, orientation),
                computeFace(level, pos, state, Direction.EAST, orientation)
        );
    }

    @Override
    protected ConnectedTextureBlockModelPart createPart(StandardCTMKey key, long overlayMask) {
        return CTMPartBuilder.create(
                baseQuads,
                unculledFaces,
                particleMaterial,
                ambientOcclusion,
                (side, faceQuads) -> {
                    if (connectedFaces.contains(side) || renderOverlayOnAllFaces) {
                        appendConnectedQuads(key, side, faceQuads);
                    }
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

    private int computeFace(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction face, CTMFaceOrientation orientation) {
        Direction[] planeDirections = orientation.cornerDirections(face);
        int packed = 0;

        for(int c = 0; c < 4; c++) {
            Direction s1 = planeDirections[c];
            Direction s2 = planeDirections[(c + 1) % 4];

            boolean firstConnected = shouldConnectSide(level, pos, state, face, s1);
            boolean secondConnected = shouldConnectSide(level, pos, state, face, s2);
            boolean corner = firstConnected && secondConnected && isCornerBlockPresent(level, pos, state, face, s1, s2);

            boolean firstIsHorizontal = s1.getAxis() == orientation.textureDirections(face)[1].getAxis();
            CTMLogic logic = firstIsHorizontal ? CTMLogic.of(firstConnected, secondConnected, corner) : CTMLogic.of(secondConnected, firstConnected, corner);
            packed |= logic.ordinal() << (c * 3);
        }

        return packed;
    }

    protected void appendConnectedQuads(StandardCTMKey key, Direction side, List<BakedQuad> faceQuads) {
        int pattern = connectedFaces.contains(side) ? key.patternIndex(side) : 0;
        CTMPartBuilder.appendPatternQuads(connectedQuads.get(side), pattern, faceQuads);
    }
}
