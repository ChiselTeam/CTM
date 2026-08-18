package io.github.chiselteam.ctm.client.baked;

import io.github.chiselteam.ctm.api.geometry.EdgesCTMKey;
import io.github.chiselteam.ctm.api.model.ConnectedTextureBlockModelPart;
import io.github.chiselteam.ctm.api.model.CTMOverlayRule;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.api.strategy.CTMKind;
import io.github.chiselteam.ctm.api.strategy.CTMLogic;
import io.github.chiselteam.ctm.client.AbstractConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.impl.model.CTMPartBuilder;
import io.github.chiselteam.ctm.impl.texture.StandardCTMOverlayTable;
import io.github.chiselteam.ctm.impl.texture.CTMLogicOrientation;
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

/**
 * Baked model implementation shared by EDGES and EDGES_FULL.
 *
 * <p>EDGES renders four independently selected CTM quadrants.
 * EDGES_FULL selects one cell from a 4x4 atlas and renders that cell
 * across the complete face.</p>
 */
public final class EdgesCTMBlockStateModel extends AbstractConnectedTextureBlockStateModel<EdgesCTMKey> {

    private static final int CONNECTION_MASK = EdgesCTMKey.TOP | EdgesCTMKey.RIGHT | EdgesCTMKey.BOTTOM | EdgesCTMKey.LEFT | EdgesCTMKey.TOP_LEFT | EdgesCTMKey.TOP_RIGHT | EdgesCTMKey.BOTTOM_RIGHT | EdgesCTMKey.BOTTOM_LEFT;

    private final CTMKind kind;
    private final StandardCTMOverlayTable regularOverlayTable;
    private final Map<Direction, BakedQuad[]> fullAtlasQuads;
    private final Map<Direction, BakedQuad[]> obscuredQuads;
    private final Map<CTMOverlayRule, Map<Direction, BakedQuad>> ruleQuads;

    public EdgesCTMBlockStateModel(Set<Direction> connectedFaces, Set<Direction> unculledFaces,
                                   boolean renderOverlayOnAllFaces, Map<Direction, BakedQuad[]> baseQuads,
                                   Map<Direction, BakedQuad[][]> regularConnectedQuads,
                                   Map<Direction, BakedQuad[]> fullAtlasQuads,
                                   Map<Direction, BakedQuad[]> obscuredQuads, TextureAtlasSprite particle,
                                   CTMVariant variant, CTMBlockPredicate connectionPredicate,
                                   List<CTMOverlayRule> overlayRules,
                                   Map<CTMOverlayRule, Map<Direction, BakedQuad>> ruleQuads) {
        super(connectedFaces, unculledFaces, renderOverlayOnAllFaces, baseQuads, particle, variant, connectionPredicate, overlayRules, computeTotalFlags(baseQuads, regularConnectedQuads, fullAtlasQuads, obscuredQuads, ruleQuads));

        this.kind = variant.kind();
        this.regularOverlayTable = new StandardCTMOverlayTable(regularConnectedQuads);
        this.fullAtlasQuads = fullAtlasQuads;
        this.obscuredQuads = obscuredQuads;
        this.ruleQuads = ruleQuads;

        if (!kind.isEdges() && !kind.isEdgesFull()) throw new IllegalArgumentException("EdgesCTMBlockStateModel requires EDGES or EDGES_FULL, got %s".formatted(kind));
    }

    @Override
    protected EdgesCTMKey computeCTMKey(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                        RandomSource random) {
        CTMLogicOrientation orientation = CTMLogicOrientation.of(state);
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
    private int computeFace(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction face, CTMLogicOrientation orientation) {
        if (!connectedFaces.contains(face))
            return 0;

        if (matches(level, pos, state, face, pos.relative(face)))
            return EdgesCTMKey.OBSCURED;

        Direction[] localDirections = CTMLogic.AXIS_PLANE_DIRECTIONS[orientation.toLocal(face).getAxis().ordinal()];
        Direction[] directions = java.util.Arrays.stream(localDirections).map(orientation::toWorld).toArray(Direction[]::new);

        Direction top = directions[0];
        Direction right = directions[1];
        Direction bottom = directions[2];
        Direction left = directions[3];

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

    /**
     * Checks both the neighbor in the current face plane and the neighbor
     * one block in front of the rendered face.
     */
    private boolean shouldConnectEdge(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction face, Direction side) {
        BlockPos sidePos = pos.relative(side);

        return matches(level, pos, state, face, sidePos) || matches(level, pos, state, face, sidePos.relative(face));
    }

    /**
     * A diagonal can connect from either the current face plane or the plane
     * directly in front.
     *
     * <p>At least one adjacent side must be visible in the current plane.</p>
     */
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

        faceQuads.addAll(regularOverlayTable.get(side, regularPatternIndex(packed)));
    }

    private void appendEdgesFullQuads(int packed, Direction side, List<BakedQuad> faceQuads) {
        CTMPartBuilder.appendIndexedQuad(fullAtlasQuads.get(side), fullAtlasIndex(packed), faceQuads);
    }

    /**
     * Converts the raw edge connection flags into the four CTMLogic values
     * expected by StandardCTMOverlayTable.
     */
    private static int regularPatternIndex(int packed) {
        boolean top = has(packed, EdgesCTMKey.TOP);
        boolean right = has(packed, EdgesCTMKey.RIGHT);
        boolean bottom = has(packed, EdgesCTMKey.BOTTOM);
        boolean left = has(packed, EdgesCTMKey.LEFT);

        CTMLogic bottomLeft = edgeLogic (bottom, left, has(packed, EdgesCTMKey.BOTTOM_LEFT), false);
        CTMLogic bottomRight = edgeLogic(bottom, right, has(packed, EdgesCTMKey.BOTTOM_RIGHT), true);
        CTMLogic topRight = edgeLogic(top, right, has(packed, EdgesCTMKey.TOP_RIGHT), false);
        CTMLogic topLeft = edgeLogic(top, left, has(packed, EdgesCTMKey.TOP_LEFT), true);

        int logicCount = CTMLogic.values().length;

        return bottomLeft.ordinal() + bottomRight.ordinal() * logicCount + topRight.ordinal() * logicCount * logicCount + topLeft.ordinal() * logicCount * logicCount * logicCount;
    }

    /**
     * When only the diagonal is connected, Edges uses the connected corner
     * submap rather than the normal empty quadrant.
     */
    private static CTMLogic edgeLogic(boolean sideA, boolean sideB, boolean diagonal,
                                      boolean swapSides) {
        if (!sideA && !sideB && diagonal)
            return CTMLogic.CORNERLESS;

        boolean joinedCorner = sideA && sideB && diagonal;

        return swapSides
                ? CTMLogic.of(sideB, sideA, joinedCorner)
                : CTMLogic.of(sideA, sideB, joinedCorner);
    }

    /**
     * Returns the row-major atlas index used by EDGES_FULL.
     */
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

    private static int computeTotalFlags(Map<Direction, BakedQuad[]> baseQuads,
                                         Map<Direction, BakedQuad[][]> regularConnectedQuads,
                                         Map<Direction, BakedQuad[]> fullAtlasQuads,
                                         Map<Direction, BakedQuad[]> obscuredQuads,
                                         Map<CTMOverlayRule, Map<Direction, BakedQuad>> ruleQuads) {
        int flags = flags(baseQuads);

        for (BakedQuad[][] faceQuads : regularConnectedQuads.values()) {
            if (faceQuads == null) continue;

            for (BakedQuad[] quadrantQuads : faceQuads)
                flags |= flags(quadrantQuads);
        }

        flags |= flags(fullAtlasQuads);
        flags |= flags(obscuredQuads);

        for (Map<Direction, BakedQuad> sideMap : ruleQuads.values()) {
            if (sideMap == null) continue;

            for (BakedQuad quad : sideMap.values()) {
                if (quad != null)
                    flags |= quad.materialInfo().flags();
            }
        }

        return flags;
    }

    private static int flags(Map<Direction, BakedQuad[]> quadsByFace) {
        int flags = 0;

        for (BakedQuad[] quads : quadsByFace.values())
            flags |= flags(quads);

        return flags;
    }

    private static int flags(BakedQuad[] quads) {
        int flags = 0;

        if (quads == null) return flags;

        for (BakedQuad quad : quads) {
            if (quad != null)
                flags |= quad.materialInfo().flags();
        }

        return flags;
    }
}
