package io.github.chiselteam.ctm.client.baked;

import io.github.chiselteam.ctm.api.model.CTMOverlayRule;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.api.strategy.CTMLogicV16;
import io.github.chiselteam.ctm.api.strategy.CTMLogicR4;
import io.github.chiselteam.ctm.api.strategy.CTMLogicR16;
import io.github.chiselteam.ctm.api.strategy.CTMKind;
import io.github.chiselteam.ctm.api.strategy.CTMLogic4x4;
import io.github.chiselteam.ctm.api.strategy.CTMLogic2x2;
import io.github.chiselteam.ctm.api.strategy.CTMLogicR9;
import io.github.chiselteam.ctm.api.texture.MultiblockQuadSelector;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.model.ConnectedTextureBlockModelPart;
import io.github.chiselteam.ctm.api.texture.MultiblockOffsetProvider;
import io.github.chiselteam.ctm.api.strategy.CTMLogic3x3;
import io.github.chiselteam.ctm.api.strategy.CTMLogicV4;
import io.github.chiselteam.ctm.api.strategy.CTMLogicV9;
import io.github.chiselteam.ctm.api.geometry.MultiblockCTMKey;
import io.github.chiselteam.ctm.client.AbstractConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.impl.model.CTMPartBuilder;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class MultiblockCTMBlockStateModel extends AbstractConnectedTextureBlockStateModel<MultiblockCTMKey> {

    protected final Map<Direction, BakedQuad[]> multiblock2x2Quads;
    protected final Map<Direction, BakedQuad[]> multiblock3x3Quads;
    protected final Map<Direction, BakedQuad[]> multiblock4x4Quads;
    private final MultiblockQuadSelector selector;
    private final Map<Direction, BakedQuad[]> effectiveBaseQuads;
    protected final Map<CTMOverlayRule, Map<Direction, BakedQuad>> ruleQuads;

    public MultiblockCTMBlockStateModel(Set<Direction> connectedFaces,
                                        Set<Direction> unculledFaces,
                                        boolean renderOverlayOnAllFaces,
                                        Map<Direction, BakedQuad[]> baseQuads,
                                        Map<Direction, BakedQuad[]> multiblock2x2Quads,
                                        Map<Direction, BakedQuad[]> multiblock3x3Quads,
                                        Map<Direction, BakedQuad[]> multiblock4x4Quads,
                                        TextureAtlasSprite particle,
                                        CTMVariant variant,
                                        CTMBlockPredicate connectionPredicate,
                                        List<CTMOverlayRule> overlayRules,
                                        Map<CTMOverlayRule, Map<Direction, BakedQuad>> ruleQuads) {
        super(connectedFaces, unculledFaces, renderOverlayOnAllFaces, baseQuads, particle, variant, connectionPredicate, overlayRules, computeTotalFlags(baseQuads, multiblock2x2Quads, multiblock3x3Quads, multiblock4x4Quads, ruleQuads));
        this.multiblock2x2Quads = multiblock2x2Quads;
        this.multiblock3x3Quads = multiblock3x3Quads;
        this.multiblock4x4Quads = multiblock4x4Quads;
        this.ruleQuads = ruleQuads;
        this.selector = createSelector(variant.kind());
        this.effectiveBaseQuads = computeEffectiveBaseQuads();
    }

    private static int computeTotalFlags(Map<Direction, BakedQuad[]> baseQuads, Map<Direction, BakedQuad[]> mb2, Map<Direction, BakedQuad[]> mb3, Map<Direction, BakedQuad[]> mb4, Map<CTMOverlayRule, Map<Direction, BakedQuad>> rules) {
        int flags = 0;
        for (BakedQuad[] quads : baseQuads.values()) {
            for (BakedQuad quad : quads) if (quad != null) flags |= quad.materialInfo().flags();
        }
        for (BakedQuad[] quads : mb2.values()) {
            for (BakedQuad quad : quads) if (quad != null) flags |= quad.materialInfo().flags();
        }
        for (BakedQuad[] quads : mb3.values()) {
            for (BakedQuad quad : quads) if (quad != null) flags |= quad.materialInfo().flags();
        }
        for (BakedQuad[] quads : mb4.values()) {
            for (BakedQuad quad : quads) if (quad != null) flags |= quad.materialInfo().flags();
        }
        for (Map<Direction, BakedQuad> sideMap : rules.values()) {
            for (BakedQuad quad : sideMap.values()) if (quad != null) flags |= quad.materialInfo().flags();
        }
        return flags;
    }

    public MultiblockCTMBlockStateModel(Set<Direction> connectedFaces,
                                        Set<Direction> unculledFaces,
                                        boolean renderOverlayOnAllFaces,
                                        Map<Direction, BakedQuad[]> baseQuads,
                                        Map<Direction, BakedQuad[]> multiblock2x2Quads,
                                        Map<Direction, BakedQuad[]> multiblock3x3Quads,
                                        Map<Direction, BakedQuad[]> multiblock4x4Quads,
                                        TextureAtlasSprite particle,
                                        CTMVariant variant) {
        super(connectedFaces, unculledFaces, renderOverlayOnAllFaces, baseQuads, particle, variant);
        this.multiblock2x2Quads = multiblock2x2Quads;
        this.multiblock3x3Quads = multiblock3x3Quads;
        this.multiblock4x4Quads = multiblock4x4Quads;
        this.ruleQuads = Map.of();
        this.selector = createSelector(variant.kind());
        this.effectiveBaseQuads = computeEffectiveBaseQuads();
    }

    @Override
    protected MultiblockCTMKey computeCTMKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        BlockPos offsetPos = MultiblockOffsetProvider.get().offsetFor(pos);

        CTMLogicR4 sharedR4 = CTMLogicR4.values()[randomIndex(pos, 4, 4)];
        CTMLogicR9 sharedR9 = CTMLogicR9.values()[randomIndex(pos, 9, 9)];
        CTMLogicR16 sharedR16 = CTMLogicR16.values()[randomIndex(pos, 16, 16)];

        int mb2x2 = 0; int mb3x3 = 0; int mb4x4 = 0;
        int v4 = 0; int v9 = 0; int v16 = 0;
        int r4 = 0; int r9 = 0; int r16 = 0;

        for(Direction face : Direction.values()) {
            mb2x2 |= MultiblockCTMKey.pack(face, CTMLogic2x2.get(pos, face));
            mb3x3 |= MultiblockCTMKey.pack(face, CTMLogic3x3.get(pos, face));
            mb4x4 |= MultiblockCTMKey.pack(face, CTMLogic4x4.get(pos, face));

            v4 |= MultiblockCTMKey.pack(face, CTMLogicV4.get(offsetPos, face));
            v9 |= MultiblockCTMKey.pack(face, CTMLogicV9.get(offsetPos, face));
            v16 |= MultiblockCTMKey.pack(face, CTMLogicV16.get(offsetPos, face));

            r4 |= MultiblockCTMKey.pack(face, sharedR4);
            r9 |= MultiblockCTMKey.pack(face, sharedR9);
            r16 |= MultiblockCTMKey.pack(face, sharedR16);
        }

        return new MultiblockCTMKey(mb2x2, mb3x3, mb4x4, v4, v9, v16, r4, r9, r16);
    }

    @Override
    protected ConnectedTextureBlockModelPart createPart(MultiblockCTMKey key, long overlayMask) {
        return CTMPartBuilder.create(
                effectiveBaseQuads,
                unculledFaces,
                particleMaterial,
                (side, faceQuads) -> {
                    if (shouldRenderMultiblockOverlay(side)) {
                        appendMultiblockQuad(key, side, faceQuads);
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
     * The multiblock tile quads are baked coplanar with the base quads and fully cover the face,
     * so the base quad must be dropped on any side where a tile quad will always render,
     * otherwise the two quads z-fight. With a water offset the tile quads are offset off-plane,
     * so the base quads must be kept, otherwise the base layer disappears.
     */
    private Map<Direction, BakedQuad[]> computeEffectiveBaseQuads() {
        if (variant.waterOffset()) {
            return baseQuads;
        }
        Map<Direction, BakedQuad[]> tileQuads = tileQuadsForKind(variant.kind());
        Map<Direction, BakedQuad[]> result = new EnumMap<>(Direction.class);
        for (Map.Entry<Direction, BakedQuad[]> entry : baseQuads.entrySet()) {
            Direction side = entry.getKey();
            if (shouldRenderMultiblockOverlay(side) && tileQuads.get(side) != null) {
                continue;
            }
            result.put(side, entry.getValue());
        }
        return result;
    }

    private Map<Direction, BakedQuad[]> tileQuadsForKind(CTMKind kind) {
        if (kind.isV4() || kind.isR4() || (kind.usesMultiblockCTM() && kind.multiblockSize() == 2)) {
            return multiblock2x2Quads;
        }
        if (kind.isV9() || kind.isR9() || (kind.usesMultiblockCTM() && kind.multiblockSize() == 3)) {
            return multiblock3x3Quads;
        }
        if (kind.isV16() || kind.isR16() || (kind.usesMultiblockCTM() && kind.multiblockSize() == 4)) {
            return multiblock4x4Quads;
        }
        return Map.of();
    }

    private boolean shouldRenderMultiblockOverlay(Direction side) {
        return isRandomVariant()
                || connectedFaces.contains(side)
                || renderOverlayOnAllFaces;
    }

    private boolean isRandomVariant() {
        return variant.kind().usesRandomTexture();
    }

    private void appendMultiblockQuad(MultiblockCTMKey key, Direction side, List<BakedQuad> faceQuads) {
        selector.append(key, side, faceQuads);
    }

    private MultiblockQuadSelector createSelector(CTMKind kind) {
        if (kind.isV4()) {
            return (key, side, faceQuads) -> CTMPartBuilder.appendIndexedQuad(
                    multiblock2x2Quads.get(side),
                    key.v4(side).ordinal(),
                    faceQuads
            );
        }
        if (kind.isV9()) {
            return (key, side, faceQuads) -> CTMPartBuilder.appendIndexedQuad(
                    multiblock3x3Quads.get(side),
                    key.v9(side).ordinal(),
                    faceQuads
            );
        }
        if (kind.isV16()) {
            return (key, side, faceQuads) -> CTMPartBuilder.appendIndexedQuad(
                    multiblock4x4Quads.get(side),
                    key.v16(side).ordinal(),
                    faceQuads
            );
        }
        if (kind.isR4()) {
            return (key, side, faceQuads) -> CTMPartBuilder.appendIndexedQuad(
                    multiblock2x2Quads.get(side),
                    key.r4(side).ordinal(),
                    faceQuads
            );
        }
        if (kind.isR9()) {
            return (key, side, faceQuads) -> CTMPartBuilder.appendIndexedQuad(
                    multiblock3x3Quads.get(side),
                    key.r9(side).ordinal(),
                    faceQuads
            );
        }
        if (kind.isR16()) {
            return (key, side, faceQuads) -> CTMPartBuilder.appendIndexedQuad(
                    multiblock4x4Quads.get(side),
                    key.r16(side).ordinal(),
                    faceQuads
            );
        }
        if (kind.usesMultiblockCTM()) {
            switch (kind.multiblockSize()) {
                case 2 -> {
                    return (key, side, faceQuads) -> CTMPartBuilder.appendIndexedQuad(
                            multiblock2x2Quads.get(side),
                            key.mb2x2(side).ordinal(),
                            faceQuads
                    );
                }
                case 3 -> {
                    return (key, side, faceQuads) -> CTMPartBuilder.appendIndexedQuad(
                            multiblock3x3Quads.get(side),
                            key.mb3x3(side).ordinal(),
                            faceQuads
                    );
                }
                case 4 -> {
                    return (key, side, faceQuads) -> CTMPartBuilder.appendIndexedQuad(
                            multiblock4x4Quads.get(side),
                            key.mb4x4(side).ordinal(),
                            faceQuads
                    );
                }
            }
        }
        return (key, side, faceQuads) -> {};
    }

    private static int randomIndex(BlockPos pos, int salt, int bound) {
        long seed = pos.asLong();
        seed ^= salt * 0x9E3779897F4A7C15L;
        seed ^= seed >>> 33;
        seed *= 0xff51afd7ed558ccdL;
        seed ^= seed >>> 33;
        seed *= 0xc4ceb9fe1a85ec53L;
        seed ^= seed >>> 33;

        return Math.floorMod((int) seed, bound);
    }
}
