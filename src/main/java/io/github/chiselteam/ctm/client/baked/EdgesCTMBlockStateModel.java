package io.github.chiselteam.ctm.client.baked;

import io.github.chiselteam.ctm.api.geometry.EdgesCTMKey;
import io.github.chiselteam.ctm.api.model.CTMOverlayRule;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.model.ConnectedTextureBlockModelPart;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.api.strategy.CTMKind;
import io.github.chiselteam.ctm.api.strategy.CTMLogic;
import io.github.chiselteam.ctm.client.AbstractConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.impl.model.CTMPartBuilder;
import io.github.chiselteam.ctm.impl.texture.CTMFaceOrientation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Consumer;
import java.util.Map;
import java.util.Set;

public final class EdgesCTMBlockStateModel extends AbstractConnectedTextureBlockStateModel<EdgesCTMKey> {

    private static final int CONNECTION_MASK = EdgesCTMKey.TOP | EdgesCTMKey.RIGHT | EdgesCTMKey.BOTTOM | EdgesCTMKey.LEFT | EdgesCTMKey.TOP_LEFT | EdgesCTMKey.TOP_RIGHT | EdgesCTMKey.BOTTOM_RIGHT | EdgesCTMKey.BOTTOM_LEFT;

    private final CTMKind kind;
    private final CTMFaceOrientation orientation;
    private final Map<Direction, BakedQuad[][]> regularConnectedQuads;
    private final Map<Direction, BakedQuad[]> fullAtlasQuads;
    private final Map<Direction, BakedQuad[]> obscuredQuads;
    private final Map<CTMOverlayRule, Map<Direction, BakedQuad>> ruleQuads;

    public EdgesCTMBlockStateModel(Set<Direction> connectedFaces, Set<Direction> unculledFaces, boolean renderOverlayOnAllFaces, Map<Direction, BakedQuad[]> baseQuads, Map<Direction, BakedQuad[][]> regularConnectedQuads, Map<Direction, BakedQuad[]> fullAtlasQuads, Map<Direction, BakedQuad[]> obscuredQuads, TextureAtlasSprite particle, CTMVariant variant, CTMBlockPredicate connectionPredicate, List<CTMOverlayRule> overlayRules, Map<CTMOverlayRule, Map<Direction, BakedQuad>> ruleQuads, boolean ambientOcclusion) {
        this(connectedFaces, unculledFaces, renderOverlayOnAllFaces, baseQuads, regularConnectedQuads, fullAtlasQuads, obscuredQuads, particle, variant, connectionPredicate, overlayRules, ruleQuads, ambientOcclusion, CTMFaceOrientation.IDENTITY);
    }

    public EdgesCTMBlockStateModel(Set<Direction> connectedFaces, Set<Direction> unculledFaces, boolean renderOverlayOnAllFaces, Map<Direction, BakedQuad[]> baseQuads, Map<Direction, BakedQuad[][]> regularConnectedQuads, Map<Direction, BakedQuad[]> fullAtlasQuads, Map<Direction, BakedQuad[]> obscuredQuads, TextureAtlasSprite particle, CTMVariant variant, CTMBlockPredicate connectionPredicate, List<CTMOverlayRule> overlayRules, Map<CTMOverlayRule, Map<Direction, BakedQuad>> ruleQuads, boolean ambientOcclusion, CTMFaceOrientation orientation) {
        super(connectedFaces, unculledFaces, renderOverlayOnAllFaces, baseQuads, particle, variant, connectionPredicate, overlayRules, ambientOcclusion);
        this.kind = variant.kind();
        this.orientation = orientation;
        this.regularConnectedQuads = regularConnectedQuads;
        this.fullAtlasQuads = fullAtlasQuads;
        this.obscuredQuads = obscuredQuads;
        this.ruleQuads = ruleQuads;
        if (!kind.isEdges() && !kind.isEdgesFull()) throw new IllegalArgumentException("EdgesCTMBlockStateModel requires EDGES or EDGES_FULL, got %s".formatted(kind));
    }

    public EdgesCTMBlockStateModel(Set<Direction> connectedFaces, Set<Direction> unculledFaces,
                                   boolean renderOverlayOnAllFaces, Map<Direction, BakedQuad[]> baseQuads,
                                   Map<Direction, BakedQuad[][]> regularConnectedQuads,
                                   Map<Direction, BakedQuad[]> fullAtlasQuads,
                                   Map<Direction, BakedQuad[]> obscuredQuads, TextureAtlasSprite particle,
                                   CTMVariant variant, CTMBlockPredicate connectionPredicate,
                                   List<CTMOverlayRule> overlayRules,
                                   Map<CTMOverlayRule, Map<Direction, BakedQuad>> ruleQuads) {
        super(connectedFaces, unculledFaces, renderOverlayOnAllFaces, baseQuads, particle, variant, connectionPredicate, overlayRules);

        this.kind = variant.kind();
        this.orientation = CTMFaceOrientation.IDENTITY;
        this.regularConnectedQuads = regularConnectedQuads;
        this.fullAtlasQuads = fullAtlasQuads;
        this.obscuredQuads = obscuredQuads;
        this.ruleQuads = ruleQuads;

        if (!kind.isEdges() && !kind.isEdgesFull()) throw new IllegalArgumentException("EdgesCTMBlockStateModel requires EDGES or EDGES_FULL, got %s".formatted(kind));
    }

    @Override
    public void visitQuadArrays(Consumer<BakedQuad[]> visitor) {
        super.visitQuadArrays(visitor);
        regularConnectedQuads.values().forEach(corners -> {
            if (corners != null) {
                for (BakedQuad[] quads : corners) visitor.accept(quads);
            }
        });
        fullAtlasQuads.values().forEach(visitor);
        obscuredQuads.values().forEach(visitor);
    }

    @Override
    protected EdgesCTMKey computeCTMKey(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                        RandomSource random) {
        return new EdgesCTMKey(
                computeFace(level, pos, state, Direction.DOWN, orientation),
                computeFace(level, pos, state, Direction.UP, orientation),
                computeFace(level, pos, state, Direction.NORTH, orientation),
                computeFace(level, pos, state, Direction.SOUTH, orientation),
                computeFace(level, pos, state, Direction.WEST, orientation),
                computeFace(level, pos, state, Direction.EAST, orientation)
        );
    }

    @Override
    protected ConnectedTextureBlockModelPart createPart(EdgesCTMKey key, long overlayMask) {
        return CTMPartBuilder.create(
                baseQuads,
                unculledFaces,
                particleMaterial,
                ambientOcclusion,
                (side, faceQuads) -> {
                    if (connectedFaces.contains(side) || renderOverlayOnAllFaces)
                        appendEdgesQuads(key, side, faceQuads);

                    appendRuleQuads(overlayMask, side, faceQuads);
                }
        );
    }

    /**
     * Computes the connection state for one rendered face.
     *
     * <pre>
     * bit 0 = top
     * bit 1 = right
     * bit 2 = bottom
     * bit 3 = left
     * bit 4 = top-left
     * bit 5 = top-right
     * bit 6 = bottom-right
     * bit 7 = bottom-left
     * bit 8 = obscured
     * </pre>
     */
    private int computeFace(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction face, CTMFaceOrientation orientation) {
        if (!connectedFaces.contains(face))
            return 0;

        if (matches(level, pos, state, face, pos.relative(face)))
            return EdgesCTMKey.OBSCURED;

        var top = orientation.textureDirection(face, 0);
        var right = orientation.textureDirection(face, 1);
        var bottom = orientation.textureDirection(face, 2);
        var left = orientation.textureDirection(face, 3);

        int packed = 0;

        if (shouldConnectEdge(level, pos, state, face, top)) packed |= EdgesCTMKey.TOP;
        if (shouldConnectEdge(level, pos, state, face, right)) packed |= EdgesCTMKey.RIGHT;
        if (shouldConnectEdge(level, pos, state, face, bottom)) packed |= EdgesCTMKey.BOTTOM;
        if (shouldConnectEdge(level, pos, state, face, left)) packed |= EdgesCTMKey.LEFT;

        if (shouldConnectDiagonal(level, pos, state, face, top, left)) packed |= EdgesCTMKey.TOP_LEFT;
        if (shouldConnectDiagonal(level, pos, state, face, top, right)) packed |= EdgesCTMKey.TOP_RIGHT;
        if (shouldConnectDiagonal(level, pos, state, face, bottom, right)) packed |= EdgesCTMKey.BOTTOM_RIGHT;
        if (shouldConnectDiagonal(level, pos, state, face, bottom, left)) packed |= EdgesCTMKey.BOTTOM_LEFT;

        return packed;
    }

    private boolean shouldConnectEdge(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction face, Direction side) {
        BlockPos sidePos = pos.relative(side);

        return matches(level, pos, state, face, sidePos) || matches(level, pos, state, face, sidePos.relative(face));
    }

    private boolean shouldConnectDiagonal(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction face, Direction sideA, Direction sideB) {
        BlockPos diagonalPos = pos.relative(sideA).relative(sideB);
        boolean diagonalMatches = matches(level, pos, state, face, diagonalPos) || matches(level, pos, state, face, diagonalPos.relative(face));

        if (!diagonalMatches) return false;

        return isVisibleSupportingEdge(level, pos, state, face, sideA) || isVisibleSupportingEdge(level, pos, state, face, sideB);
    }

    private boolean isVisibleSupportingEdge(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction face, Direction side) {
        BlockPos sidePos = pos.relative(side);
        BlockState sideState = level.getBlockState(sidePos);
        BlockState sideAppearance = sideState.getAppearance(level, sidePos, face, state, pos);

        if (sideAppearance != state) return false;

        return !matches(level, pos, state, face, sidePos.relative(face));
    }

    private void appendEdgesQuads(EdgesCTMKey key, Direction side, List<BakedQuad> faceQuads) {
        int packed = connectedFaces.contains(side) ? key.packedFace(side) : 0;

        if (kind.isEdgesFull()) {
            appendEdgesFullQuads(packed, side, faceQuads);
            return;
        }

        appendRegularEdgesQuads(packed, side, faceQuads);
    }

    private void appendRegularEdgesQuads(int packed, Direction side, List<BakedQuad> faceQuads) {
        if ((packed & EdgesCTMKey.OBSCURED) != 0) {
            CTMPartBuilder.appendIndexedQuad(obscuredQuads.get(side), 0, faceQuads);
            return;
        }

        CTMPartBuilder.appendPatternQuads(regularConnectedQuads.get(side), regularPatternIndex(packed, side), faceQuads);
    }

    private void appendEdgesFullQuads(int packed, Direction side, List<BakedQuad> faceQuads) {
        CTMPartBuilder.appendIndexedQuad(fullAtlasQuads.get(side), fullAtlasIndex(packed), faceQuads);
    }

    private int regularPatternIndex(int packed, Direction face) {
        boolean top = has(packed, EdgesCTMKey.TOP);
        boolean right = has(packed, EdgesCTMKey.RIGHT);
        boolean bottom = has(packed, EdgesCTMKey.BOTTOM);
        boolean left = has(packed, EdgesCTMKey.LEFT);

        boolean[] sides = {top, right, bottom, left};
        int[] diagonals = {EdgesCTMKey.TOP_RIGHT, EdgesCTMKey.BOTTOM_RIGHT, EdgesCTMKey.BOTTOM_LEFT, EdgesCTMKey.TOP_LEFT};
        int pattern = 0;
        int multiplier = 1;
        for (int c = 0; c < 4; c++) {
            int a = orientation.textureIndex(face, orientation.cornerDirection(face, c));
            int b = orientation.textureIndex(face, orientation.cornerDirection(face, (c + 1) % 4));
            int diagonal = (a + 1) % 4 == b ? a : b;
            CTMLogic logic = edgeLogic(sides[a], sides[b], has(packed, diagonals[diagonal]), a % 2 == 0);
            pattern += logic.ordinal() * multiplier;
            multiplier *= CTMLogic.values().length;
        }
        return pattern;
    }

    private static CTMLogic edgeLogic(boolean sideA, boolean sideB, boolean diagonal,
                                      boolean swapSides) {
        if (!sideA && !sideB && diagonal)
            return CTMLogic.CORNERLESS;

        boolean joinedCorner = sideA && sideB && diagonal;

        return swapSides
                ? CTMLogic.of(sideB, sideA, joinedCorner)
                : CTMLogic.of(sideA, sideB, joinedCorner);
    }

    private static int fullAtlasIndex(int packed) {
        if ((packed & EdgesCTMKey.OBSCURED) != 0)
            return cell(2, 1);

        boolean directTop = has(packed, EdgesCTMKey.TOP);
        boolean directRight = has(packed, EdgesCTMKey.RIGHT);
        boolean directBottom = has(packed, EdgesCTMKey.BOTTOM);
        boolean directLeft = has(packed, EdgesCTMKey.LEFT);

        boolean topLeft = has(packed, EdgesCTMKey.TOP_LEFT);
        boolean topRight = has(packed, EdgesCTMKey.TOP_RIGHT);
        boolean bottomRight = has(packed, EdgesCTMKey.BOTTOM_RIGHT);
        boolean bottomLeft = has(packed, EdgesCTMKey.BOTTOM_LEFT);

        boolean top = directTop || topLeft && topRight;
        boolean right = directRight || topRight && bottomRight;
        boolean bottom = directBottom || bottomLeft && bottomRight;
        boolean left = directLeft || topLeft && bottomLeft;

        if ((packed & CONNECTION_MASK) == 0) return cell(0, 0);
        if (top && bottom || right && left) return cell(2, 1);

        boolean noCardinals = !(top || right || bottom || left);

        if (noCardinals && topLeft && bottomRight) return cell(0, 1);
        if (noCardinals && topRight && bottomLeft) return cell(0, 2);

        if (!(bottom || right) && (directLeft || bottomLeft) && (directTop || topRight)) return cell(0, 3);
        if (!(bottom || left) && (directTop || topLeft) && (directRight || bottomRight)) return cell(1, 3);
        if (!(top || left) && (directRight || topRight) && (directBottom || bottomLeft)) return cell(2, 3);
        if (!(top || right) && (directBottom || bottomRight) && (directLeft || topLeft)) return cell(3, 3);

        if (bottom) return cell(1, 1);
        if (right) return cell(2, 0);
        if (left) return cell(2, 2);
        if (top) return cell(3, 1);

        if (bottomLeft) return cell(1, 2);
        if (bottomRight) return cell(1, 0);
        if (topRight) return cell(3, 0);
        if (topLeft) return cell(3, 2);

        return cell(0, 0);
    }

    private static boolean has(int packed, int flag) {
        return (packed & flag) != 0;
    }

    private static int cell(int row, int column) {
        return row * 4 + column;
    }

    private void appendRuleQuads(long mask, Direction side, List<BakedQuad> faceQuads) {
        if (mask == 0) return;

        int ruleCount = Math.min(overlayRules.size(), 10);

        for (int i = 0; i < ruleCount; i++) {
            long faceBit = 1L << (i * 6 + side.ordinal());

            if ((mask & faceBit) == 0) continue;

            CTMOverlayRule rule = overlayRules.get(i);
            Map<Direction, BakedQuad> quads = ruleQuads.get(rule);

            if (quads == null) continue;

            BakedQuad quad = quads.get(side);

            if (quad != null) faceQuads.add(quad);
        }
    }
}
