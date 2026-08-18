package io.github.chiselteam.ctm.client.baked;

import io.github.chiselteam.ctm.api.model.CTMOverlayRule;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.api.strategy.CTMKind;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.CTMLogicHorizontal;
import io.github.chiselteam.ctm.api.strategy.CTMLogicVertical;
import io.github.chiselteam.ctm.api.model.ConnectedTextureBlockModelPart;
import io.github.chiselteam.ctm.api.geometry.DirectionalCTMKey;
import io.github.chiselteam.ctm.client.AbstractConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.impl.model.CTMPartBuilder;
import io.github.chiselteam.ctm.impl.texture.CTMLogicOrientation;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class DirectionalCTMBlockStateModel extends AbstractConnectedTextureBlockStateModel<DirectionalCTMKey> {

    protected final Map<Direction, BakedQuad[]> horizontalQuads;
    protected final Map<Direction, BakedQuad[]> verticalQuads;
    protected final Map<CTMOverlayRule, Map<Direction, BakedQuad>> ruleQuads;
    private final Map<Direction, BakedQuad[]> effectiveBaseQuads;

    public DirectionalCTMBlockStateModel(Set<Direction> connectedFaces,
                                         Set<Direction> unculledFaces,
                                         boolean renderOverlayOnAllFaces,
                                         Map<Direction, BakedQuad[]> baseQuads,
                                         Map<Direction, BakedQuad[]> horizontalQuads,
                                         Map<Direction, BakedQuad[]> verticalQuads,
                                         TextureAtlasSprite particle,
                                         CTMVariant variant,
                                         CTMBlockPredicate connectionPredicate,
                                         List<CTMOverlayRule> overlayRules,
                                         Map<CTMOverlayRule, Map<Direction, BakedQuad>> ruleQuads) {
        super(connectedFaces, unculledFaces, renderOverlayOnAllFaces, baseQuads, particle, variant, connectionPredicate, overlayRules, computeTotalFlags(baseQuads, horizontalQuads, verticalQuads, ruleQuads));
        this.horizontalQuads = horizontalQuads;
        this.verticalQuads = verticalQuads;
        this.ruleQuads = ruleQuads;
        this.effectiveBaseQuads = computeEffectiveBaseQuads();
    }

    private static int computeTotalFlags(Map<Direction, BakedQuad[]> baseQuads, Map<Direction, BakedQuad[]> horizontalQuads, Map<Direction, BakedQuad[]> verticalQuads, Map<CTMOverlayRule, Map<Direction, BakedQuad>> ruleQuads) {
        int flags = 0;
        for (BakedQuad[] quads : baseQuads.values()) {
            for (BakedQuad quad : quads) if (quad != null) flags |= quad.materialInfo().flags();
        }
        for (BakedQuad[] quads : horizontalQuads.values()) {
            for (BakedQuad quad : quads) if (quad != null) flags |= quad.materialInfo().flags();
        }
        for (BakedQuad[] quads : verticalQuads.values()) {
            for (BakedQuad quad : quads) if (quad != null) flags |= quad.materialInfo().flags();
        }
        for (Map<Direction, BakedQuad> sideMap : ruleQuads.values()) {
            for (BakedQuad quad : sideMap.values()) if (quad != null) flags |= quad.materialInfo().flags();
        }
        return flags;
    }

    public DirectionalCTMBlockStateModel(Set<Direction> connectedFaces,
                                         Set<Direction> unculledFaces,
                                         boolean renderOverlayOnAllFaces,
                                         Map<Direction, BakedQuad[]> baseQuads,
                                         Map<Direction, BakedQuad[]> horizontalQuads,
                                         Map<Direction, BakedQuad[]> verticalQuads,
                                         TextureAtlasSprite particle,
                                         CTMVariant variant) {
        super(connectedFaces, unculledFaces, renderOverlayOnAllFaces, baseQuads, particle, variant);
        this.horizontalQuads = horizontalQuads;
        this.verticalQuads = verticalQuads;
        this.ruleQuads = Map.of();
        this.effectiveBaseQuads = computeEffectiveBaseQuads();
    }

    @Override
    protected DirectionalCTMKey computeCTMKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        int horizontal = 0;
        int vertical = 0;
        CTMLogicOrientation orientation = CTMLogicOrientation.of(state);

        for (Direction face : Direction.values()) {
            Direction localFace = orientation.toLocal(face);
            Direction.Axis axis = localFace.getAxis();

            CTMLogicHorizontal horizontalLogic;
            CTMLogicVertical verticalLogic;

            if (axis == Direction.Axis.Y) {
                horizontalLogic = CTMLogicHorizontal.get(shouldConnectSide(level, pos, state, face, orientation.toWorld(Direction.WEST)), shouldConnectSide(level, pos, state, face, orientation.toWorld(Direction.EAST)));
                verticalLogic = CTMLogicVertical.get(shouldConnectSide(level, pos, state, face, orientation.toWorld(Direction.NORTH)), shouldConnectSide(level, pos, state, face, orientation.toWorld(Direction.SOUTH)));
            } else {
                Direction horizontalDir = localFace.getClockWise();
                horizontalLogic = CTMLogicHorizontal.get(shouldConnectSide(level, pos, state, face, orientation.toWorld(horizontalDir.getOpposite())), shouldConnectSide(level, pos, state, face, orientation.toWorld(horizontalDir)));
                verticalLogic = CTMLogicVertical.get(shouldConnectSide(level, pos, state, face, orientation.toWorld(Direction.UP)), shouldConnectSide(level, pos, state, face, orientation.toWorld(Direction.DOWN)));
            }

            horizontal |= DirectionalCTMKey.packHorizontal(face, horizontalLogic);
            vertical |= DirectionalCTMKey.packVertical(face, verticalLogic);
        }

        return new DirectionalCTMKey(horizontal, vertical);
    }

    @Override
    protected ConnectedTextureBlockModelPart createPart(DirectionalCTMKey key, long overlayMask) {
        return CTMPartBuilder.create(
                effectiveBaseQuads,
                unculledFaces,
                particleMaterial,
                (side, faceQuads) -> {
                    if (shouldRenderDirectionalOverlay(side)) {
                        appendDirectionalQuad(key, side, faceQuads);
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

    /**
     * The CTMH/CTMV tile quads are baked coplanar with the base quads and fully cover the face,
     * so the base quad must be dropped on any side where a directional tile quad will always render,
     * otherwise the two quads z-fight. Bookshelf-like kinds are inset and keep their base quads.
     * With a water offset the tile quads are offset off-plane, so the base quads must be kept,
     * otherwise the base layer disappears.
     */
    private Map<Direction, BakedQuad[]> computeEffectiveBaseQuads() {
        if (variant.waterOffset()) {
            return baseQuads;
        }
        CTMKind kind = variant.kind();
        Map<Direction, BakedQuad[]> tileQuads;
        if (kind.isCTMH()) {
            tileQuads = horizontalQuads;
        } else if (kind.isCTMV()) {
            tileQuads = verticalQuads;
        } else {
            return baseQuads;
        }
        Map<Direction, BakedQuad[]> result = new EnumMap<>(Direction.class);
        for (Map.Entry<Direction, BakedQuad[]> entry : baseQuads.entrySet()) {
            Direction side = entry.getKey();
            if (shouldRenderDirectionalOverlay(side) && tileQuads.get(side) != null) {
                continue;
            }
            result.put(side, entry.getValue());
        }
        return result;
    }

    private boolean shouldRenderDirectionalOverlay(Direction side) {
        CTMKind kind = variant.kind();
        if (kind.isBookshelfLike()) {
            return side.getAxis().isHorizontal() && horizontalQuads.containsKey(side);
        } else if (kind.usesDirectionalCTM()) {
            return connectedFaces.contains(side) || renderOverlayOnAllFaces;
        }
        return false;
    }

    private void appendDirectionalQuad(DirectionalCTMKey key, Direction side, List<BakedQuad> faceQuads) {
        CTMKind kind = variant.kind();
        if (kind.isBookshelfLike() || kind.isCTMH()) {
            CTMLogicHorizontal logic = key.horizontal(side);
            CTMPartBuilder.appendIndexedQuad(horizontalQuads.get(side), logic.ordinal(), faceQuads);
        } else if (kind.isCTMV()) {
            CTMLogicVertical logic = key.vertical(side);
            CTMPartBuilder.appendIndexedQuad(verticalQuads.get(side), logic.ordinal(), faceQuads);
        }
    }
}
