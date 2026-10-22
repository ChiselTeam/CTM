package io.github.chiselteam.ctm.api.datagen;

import com.mojang.datafixers.util.Pair;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.api.strategy.CTMKind;
import io.github.chiselteam.ctm.api.strategy.CTMLogicEdges;
import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;
import io.github.chiselteam.ctm.client.unbaked.CTMModelCodecs;
import io.github.chiselteam.ctm.client.unbaked.UnbakedConnectedTextureBlockStateModel;
import net.minecraft.data.models.model.TextureSlot;
import io.github.chiselteam.ctm.client.model.CTMModelVariant;
import io.github.chiselteam.ctm.api.datagen.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import io.github.chiselteam.ctm.api.datagen.UnbakedMutator;
import org.joml.Vector3f;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Datagen-side builder for {@link UnbakedConnectedTextureBlockStateModel}s.
 * This class helps generate CTM model JSON using the normal NeoForge datagen flow.
 *
 * <p>Example usage:
 * <pre>{@code
 * CTMModelBuilder.standard(block, modelLocation)
 *     .connectedFace(Direction.NORTH)
 *     .connectedFace(Direction.SOUTH)
 *     .toUnbaked();
 * }</pre>
 */
public class CTMModelBuilder {

    private final Block block;
    private final CTMKind kind;
    private ResourceLocation modelLocation;
    private Pair<Vector3f, Vector3f> element = Pair.of(new Vector3f(0, 0, 0), new Vector3f(16, 16, 16));
    private final EnumSet<Direction> connectedFaces = EnumSet.noneOf(Direction.class);
    private boolean renderOverlayOnAllFaces = false;
    private int baseTintIndex = -1;
    private int baseEmissivity = 0;
    private int tintIndex = -1;
    private int emissivity = 0;
    private boolean shade = true;
    private boolean ambientOcclusion = true;
    private boolean eldritch = false;
    private boolean waterOffset = false;
    private CTMBlockPredicate connectionPredicate = CTMBlockPredicate.sameBlock();
    private final List<CTMModelCodecs.UnbakedOverlayRule> overlays = new ArrayList<>();
    private final Map<String, ResourceLocation> textureSlots = new HashMap<>();
    private CTMModelVariant.SimpleModelState modelState = CTMModelVariant.SimpleModelState.DEFAULT;
    private final List<UnbakedMutator> unbakedMutators = new ArrayList<>();

    protected CTMModelBuilder(Block block, CTMKind kind) {
        this.block = block;
        this.kind = kind;
    }

    /**
     * Creates a builder for any CTM kind.
     */
    public static CTMModelBuilder of(Block block, CTMKind kind, ResourceLocation modelLocation) {
        return new CTMModelBuilder(block, kind).modelLocation(modelLocation);
    }

    /** Composes CTM builders in bottom-to-top draw order. */
    public static LayeredCTMModelBuilder layered(CTMModelBuilder bottom, CTMModelBuilder... upperLayers) {
        return LayeredCTMModelBuilder.of(bottom, upperLayers);
    }

    /**
     * Creates a builder for a standard 5-bit CTM model.
     */
    public static CTMModelBuilder standard(Block block, ResourceLocation modelLocation) {
        return new CTMModelBuilder(block, CTMKind.STANDARD).modelLocation(modelLocation);
    }

    /**
     * Creates a builder for a top/bottom/side (TBS) CTM model.
     */
    public static CTMModelBuilder tbs(Block block, ResourceLocation modelLocation) {
        return new CTMModelBuilder(block, CTMKind.TBS).modelLocation(modelLocation);
    }

    /**
     * Creates a builder for a horizontal CTM model.
     */
    public static CTMModelBuilder horizontal(Block block, ResourceLocation modelLocation) {
        return new CTMModelBuilder(block, CTMKind.CTMH).modelLocation(modelLocation);
    }

    /**
     * Creates a builder for a vertical CTM model.
     */
    public static CTMModelBuilder vertical(Block block, ResourceLocation modelLocation) {
        return new CTMModelBuilder(block, CTMKind.CTMV).modelLocation(modelLocation);
    }

    /**
     * Creates a builder for a bookshelf-like CTM model.
     */
    public static CTMModelBuilder bookshelf(Block block, ResourceLocation modelLocation) {
        return new CTMModelBuilder(block, CTMKind.BOOKSHELF).modelLocation(modelLocation);
    }

    /**
     * Creates a builder for a 2x2 multiblock CTM model.
     */
    public static CTMModelBuilder multiblock2x2(Block block, ResourceLocation modelLocation) {
        return new CTMModelBuilder(block, CTMKind.MULTIBLOCK_2X2).modelLocation(modelLocation);
    }

    /**
     * Creates a builder for a 3x3 multiblock CTM model.
     */
    public static CTMModelBuilder multiblock3x3(Block block, ResourceLocation modelLocation) {
        return new CTMModelBuilder(block, CTMKind.MULTIBLOCK_3X3).modelLocation(modelLocation);
    }

    /**
     * Creates a builder for a 4x4 multiblock CTM model.
     */
    public static CTMModelBuilder multiblock4x4(Block block, ResourceLocation modelLocation) {
        return new CTMModelBuilder(block, CTMKind.MULTIBLOCK_4X4).modelLocation(modelLocation);
    }

    /**
     * Creates a builder for an anti-repeat (AR) CTM model.
     */
    public static CTMModelBuilder ar(Block block, ResourceLocation modelLocation) {
        return new CTMModelBuilder(block, CTMKind.AR).modelLocation(modelLocation);
    }

    /**
     * Creates a builder for a regular-edge CTM model.
     *
     * <p>Regular EDGES divides each face into four quadrants and chooses the
     * connected region independently for each quadrant.
     */
    public static CTMModelBuilder edges(
            Block block,
            ResourceLocation modelLocation
    ) {
        return new CTMModelBuilder(
                block,
                CTMKind.EDGES
        ).modelLocation(modelLocation);
    }

    /**
     * Creates a builder for a full-face-edge CTM model.
     *
     * <p>EDGES_FULL chooses one of sixteen edge-state textures for the complete face.
     * All sixteen standalone texture states are required.
     */
    public static CTMModelBuilder edgesFull(
            Block block,
            ResourceLocation modelLocation
    ) {
        return new CTMModelBuilder(
                block,
                CTMKind.EDGES_FULL
        ).modelLocation(modelLocation);
    }

    public CTMModelBuilder modelLocation(ResourceLocation modelLocation) {
        this.modelLocation = modelLocation;
        return this;
    }

    public CTMModelBuilder element(Vector3f min, Vector3f max) {
        this.element = Pair.of(min, max);
        return this;
    }

    public CTMModelBuilder connectedFace(Direction direction) {
        this.connectedFaces.add(direction);
        return this;
    }

    /** Enables CTM connection logic on all six faces. */
    public CTMModelBuilder allFaces() {
        this.connectedFaces.addAll(EnumSet.allOf(Direction.class));
        return this;
    }

    public CTMModelBuilder renderOverlayOnAllFaces(boolean renderOverlayOnAllFaces) {
        this.renderOverlayOnAllFaces = renderOverlayOnAllFaces;
        return this;
    }

    public CTMModelBuilder baseTintIndex(int baseTintIndex) {
        this.baseTintIndex = baseTintIndex;
        return this;
    }

    public CTMModelBuilder baseEmissivity(int baseEmissivity) {
        this.baseEmissivity = baseEmissivity;
        return this;
    }

    public CTMModelBuilder tintIndex(int tintIndex) {
        this.tintIndex = tintIndex;
        return this;
    }

    public CTMModelBuilder emissivity(int emissivity) {
        this.emissivity = emissivity;
        return this;
    }

    /** Controls directional diffuse shading for every generated quad. */
    public CTMModelBuilder shade(boolean shade) {
        this.shade = shade;
        return this;
    }

    public CTMModelBuilder ambientOcclusion(boolean ambientOcclusion) {
        this.ambientOcclusion = ambientOcclusion;
        return this;
    }

    public CTMModelBuilder eldritch(boolean eldritch) {
        this.eldritch = eldritch;
        return this;
    }

    public CTMModelBuilder waterOffset(boolean waterOffset) {
        this.waterOffset = waterOffset;
        return this;
    }

    public CTMModelBuilder connectionPredicate(CTMBlockPredicate connectionPredicate) {
        this.connectionPredicate = connectionPredicate;
        return this;
    }

    public CTMModelBuilder overlay(CTMModelCodecs.UnbakedOverlayRule overlay) {
        this.overlays.add(overlay);
        return this;
    }

    public CTMModelBuilder texture(String slot, ResourceLocation texture) {
        this.textureSlots.put(slot, texture);
        return this;
    }

    public CTMModelBuilder texture(TextureSlot slot, ResourceLocation texture) {
        return texture(slot.getId(), texture);
    }

    /**
     * Supplies standalone Standard states for STANDARD or regular EDGES.
     */
    public CTMModelBuilder standardTextures(ResourceLocation none, ResourceLocation cornerless, ResourceLocation vertical, ResourceLocation horizontal, ResourceLocation corner) {
        return texture(CTMTextureKeys.STANDARD_NONE, none)
                .texture(CTMTextureKeys.STANDARD_CORNERLESS, cornerless)
                .texture(CTMTextureKeys.STANDARD_VERTICAL, vertical)
                .texture(CTMTextureKeys.STANDARD_HORIZONTAL, horizontal)
                .texture(CTMTextureKeys.STANDARD_CORNER, corner);
    }

    public CTMModelBuilder standardTextures(ResourceLocation base) {
        var namespace = base.getNamespace();
        var path = base.getPath();

        return standardTextures(
                ResourceLocation.fromNamespaceAndPath(namespace, "%s_none".formatted(path)),
                ResourceLocation.fromNamespaceAndPath(namespace, "%s_cornerless".formatted(path)),
                ResourceLocation.fromNamespaceAndPath(namespace, "%s_vertical".formatted(path)),
                ResourceLocation.fromNamespaceAndPath(namespace, "%s_horizontal".formatted(path)),
                ResourceLocation.fromNamespaceAndPath(namespace, "%s_corner".formatted(path))
        );
    }

    /**
     * Supplies horizontal standalone textures in the listed parameter order.
     */
    public CTMModelBuilder horizontalTextures(ResourceLocation none, ResourceLocation both, ResourceLocation left, ResourceLocation right) {
        return texture(CTMTextureKeys.HORIZONTAL_NONE, none)
                .texture(CTMTextureKeys.HORIZONTAL_BOTH, both)
                .texture(CTMTextureKeys.HORIZONTAL_LEFT, left)
                .texture(CTMTextureKeys.HORIZONTAL_RIGHT, right);
    }

    /**
     * Resolves textures as base path plus _none, _both, _left, _right.
     */
    public CTMModelBuilder horizontalTextures(ResourceLocation base) {
        return horizontalTextures(suffixedTexture(base, "none"), suffixedTexture(base, "both"), suffixedTexture(base, "left"), suffixedTexture(base, "right"));
    }

    /**
     * Supplies vertical standalone textures in the listed parameter order.
     */
    public CTMModelBuilder verticalTextures(ResourceLocation none, ResourceLocation both, ResourceLocation top, ResourceLocation bottom) {
        return texture(CTMTextureKeys.VERTICAL_NONE, none)
                .texture(CTMTextureKeys.VERTICAL_BOTH, both)
                .texture(CTMTextureKeys.VERTICAL_TOP, top)
                .texture(CTMTextureKeys.VERTICAL_BOTTOM, bottom);
    }

    /**
     * Resolves textures as base path plus _none, _both, _top, _bottom.
     */
    public CTMModelBuilder verticalTextures(ResourceLocation base) {
        return verticalTextures(suffixedTexture(base, "none"), suffixedTexture(base, "both"), suffixedTexture(base, "top"), suffixedTexture(base, "bottom"));
    }

    /**
     * Supplies multiblock_2x2 standalone textures in the listed parameter order.
     */
    public CTMModelBuilder multiblock2x2Textures(ResourceLocation topLeft, ResourceLocation topRight, ResourceLocation bottomLeft, ResourceLocation bottomRight) {
        return texture(CTMTextureKeys.MULTIBLOCK_2X2_TOP_LEFT, topLeft)
                .texture(CTMTextureKeys.MULTIBLOCK_2X2_TOP_RIGHT, topRight)
                .texture(CTMTextureKeys.MULTIBLOCK_2X2_BOTTOM_LEFT, bottomLeft)
                .texture(CTMTextureKeys.MULTIBLOCK_2X2_BOTTOM_RIGHT, bottomRight);
    }

    /**
     * Resolves textures as base path plus _top_left, _top_right, _bottom_left, _bottom_right.
     */
    public CTMModelBuilder multiblock2x2Textures(ResourceLocation base) {
        return multiblock2x2Textures(suffixedTexture(base, "top_left"), suffixedTexture(base, "top_right"), suffixedTexture(base, "bottom_left"), suffixedTexture(base, "bottom_right"));
    }

    /**
     * Supplies multiblock_3x3 standalone textures in the listed parameter order.
     */
    public CTMModelBuilder multiblock3x3Textures(ResourceLocation topLeft, ResourceLocation topCenter, ResourceLocation topRight, ResourceLocation centerLeft, ResourceLocation center, ResourceLocation centerRight, ResourceLocation bottomLeft, ResourceLocation bottomCenter, ResourceLocation bottomRight) {
        return texture(CTMTextureKeys.MULTIBLOCK_3X3_TOP_LEFT, topLeft)
                .texture(CTMTextureKeys.MULTIBLOCK_3X3_TOP_CENTER, topCenter)
                .texture(CTMTextureKeys.MULTIBLOCK_3X3_TOP_RIGHT, topRight)
                .texture(CTMTextureKeys.MULTIBLOCK_3X3_CENTER_LEFT, centerLeft)
                .texture(CTMTextureKeys.MULTIBLOCK_3X3_CENTER, center)
                .texture(CTMTextureKeys.MULTIBLOCK_3X3_CENTER_RIGHT, centerRight)
                .texture(CTMTextureKeys.MULTIBLOCK_3X3_BOTTOM_LEFT, bottomLeft)
                .texture(CTMTextureKeys.MULTIBLOCK_3X3_BOTTOM_CENTER, bottomCenter)
                .texture(CTMTextureKeys.MULTIBLOCK_3X3_BOTTOM_RIGHT, bottomRight);
    }

    /**
     * Resolves textures as base path plus _top_left, _top_center, _top_right, _center_left, _center, _center_right, _bottom_left, _bottom_center, _bottom_right.
     */
    public CTMModelBuilder multiblock3x3Textures(ResourceLocation base) {
        return multiblock3x3Textures(suffixedTexture(base, "top_left"), suffixedTexture(base, "top_center"), suffixedTexture(base, "top_right"), suffixedTexture(base, "center_left"), suffixedTexture(base, "center"), suffixedTexture(base, "center_right"), suffixedTexture(base, "bottom_left"), suffixedTexture(base, "bottom_center"), suffixedTexture(base, "bottom_right"));
    }

    /**
     * Supplies multiblock_4x4 standalone textures in the listed parameter order.
     */
    public CTMModelBuilder multiblock4x4Textures(ResourceLocation row0Column0, ResourceLocation row0Column1, ResourceLocation row0Column2, ResourceLocation row0Column3, ResourceLocation row1Column0, ResourceLocation row1Column1, ResourceLocation row1Column2, ResourceLocation row1Column3, ResourceLocation row2Column0, ResourceLocation row2Column1, ResourceLocation row2Column2, ResourceLocation row2Column3, ResourceLocation row3Column0, ResourceLocation row3Column1, ResourceLocation row3Column2, ResourceLocation row3Column3) {
        return texture(CTMTextureKeys.MULTIBLOCK_4X4_ROW_0_COLUMN_0, row0Column0)
                .texture(CTMTextureKeys.MULTIBLOCK_4X4_ROW_0_COLUMN_1, row0Column1)
                .texture(CTMTextureKeys.MULTIBLOCK_4X4_ROW_0_COLUMN_2, row0Column2)
                .texture(CTMTextureKeys.MULTIBLOCK_4X4_ROW_0_COLUMN_3, row0Column3)
                .texture(CTMTextureKeys.MULTIBLOCK_4X4_ROW_1_COLUMN_0, row1Column0)
                .texture(CTMTextureKeys.MULTIBLOCK_4X4_ROW_1_COLUMN_1, row1Column1)
                .texture(CTMTextureKeys.MULTIBLOCK_4X4_ROW_1_COLUMN_2, row1Column2)
                .texture(CTMTextureKeys.MULTIBLOCK_4X4_ROW_1_COLUMN_3, row1Column3)
                .texture(CTMTextureKeys.MULTIBLOCK_4X4_ROW_2_COLUMN_0, row2Column0)
                .texture(CTMTextureKeys.MULTIBLOCK_4X4_ROW_2_COLUMN_1, row2Column1)
                .texture(CTMTextureKeys.MULTIBLOCK_4X4_ROW_2_COLUMN_2, row2Column2)
                .texture(CTMTextureKeys.MULTIBLOCK_4X4_ROW_2_COLUMN_3, row2Column3)
                .texture(CTMTextureKeys.MULTIBLOCK_4X4_ROW_3_COLUMN_0, row3Column0)
                .texture(CTMTextureKeys.MULTIBLOCK_4X4_ROW_3_COLUMN_1, row3Column1)
                .texture(CTMTextureKeys.MULTIBLOCK_4X4_ROW_3_COLUMN_2, row3Column2)
                .texture(CTMTextureKeys.MULTIBLOCK_4X4_ROW_3_COLUMN_3, row3Column3);
    }

    /**
     * Resolves textures as base path plus _row_0_column_0, _row_0_column_1, _row_0_column_2, _row_0_column_3, _row_1_column_0, _row_1_column_1, _row_1_column_2, _row_1_column_3, _row_2_column_0, _row_2_column_1, _row_2_column_2, _row_2_column_3, _row_3_column_0, _row_3_column_1, _row_3_column_2, _row_3_column_3.
     */
    public CTMModelBuilder multiblock4x4Textures(ResourceLocation base) {
        return multiblock4x4Textures(suffixedTexture(base, "row_0_column_0"), suffixedTexture(base, "row_0_column_1"), suffixedTexture(base, "row_0_column_2"), suffixedTexture(base, "row_0_column_3"), suffixedTexture(base, "row_1_column_0"), suffixedTexture(base, "row_1_column_1"), suffixedTexture(base, "row_1_column_2"), suffixedTexture(base, "row_1_column_3"), suffixedTexture(base, "row_2_column_0"), suffixedTexture(base, "row_2_column_1"), suffixedTexture(base, "row_2_column_2"), suffixedTexture(base, "row_2_column_3"), suffixedTexture(base, "row_3_column_0"), suffixedTexture(base, "row_3_column_1"), suffixedTexture(base, "row_3_column_2"), suffixedTexture(base, "row_3_column_3"));
    }

    /**
     * Supplies ar standalone textures in the listed parameter order.
     */
    public CTMModelBuilder arTextures(ResourceLocation texture1, ResourceLocation texture2, ResourceLocation texture3, ResourceLocation texture4) {
        return texture(CTMTextureKeys.AR_1, texture1)
                .texture(CTMTextureKeys.AR_2, texture2)
                .texture(CTMTextureKeys.AR_3, texture3)
                .texture(CTMTextureKeys.AR_4, texture4);
    }

    /**
     * Resolves textures as base path plus _1, _2, _3, _4.
     */
    public CTMModelBuilder arTextures(ResourceLocation base) {
        return arTextures(suffixedTexture(base, "1"), suffixedTexture(base, "2"), suffixedTexture(base, "3"), suffixedTexture(base, "4"));
    }

    /**
     * Supplies standard_top standalone textures in the listed parameter order.
     */
    public CTMModelBuilder tbsTopTextures(ResourceLocation none, ResourceLocation cornerless, ResourceLocation vertical, ResourceLocation horizontal, ResourceLocation corner) {
        return texture(CTMTextureKeys.STANDARD_TOP_NONE, none)
                .texture(CTMTextureKeys.STANDARD_TOP_CORNERLESS, cornerless)
                .texture(CTMTextureKeys.STANDARD_TOP_VERTICAL, vertical)
                .texture(CTMTextureKeys.STANDARD_TOP_HORIZONTAL, horizontal)
                .texture(CTMTextureKeys.STANDARD_TOP_CORNER, corner);
    }

    /**
     * Resolves textures as base path plus _none, _cornerless, _vertical, _horizontal, _corner.
     */
    public CTMModelBuilder tbsTopTextures(ResourceLocation base) {
        return tbsTopTextures(suffixedTexture(base, "none"), suffixedTexture(base, "cornerless"), suffixedTexture(base, "vertical"), suffixedTexture(base, "horizontal"), suffixedTexture(base, "corner"));
    }

    /**
     * Supplies standard_bottom standalone textures in the listed parameter order.
     */
    public CTMModelBuilder tbsBottomTextures(ResourceLocation none, ResourceLocation cornerless, ResourceLocation vertical, ResourceLocation horizontal, ResourceLocation corner) {
        return texture(CTMTextureKeys.STANDARD_BOTTOM_NONE, none)
                .texture(CTMTextureKeys.STANDARD_BOTTOM_CORNERLESS, cornerless)
                .texture(CTMTextureKeys.STANDARD_BOTTOM_VERTICAL, vertical)
                .texture(CTMTextureKeys.STANDARD_BOTTOM_HORIZONTAL, horizontal)
                .texture(CTMTextureKeys.STANDARD_BOTTOM_CORNER, corner);
    }

    /**
     * Resolves textures as base path plus _none, _cornerless, _vertical, _horizontal, _corner.
     */
    public CTMModelBuilder tbsBottomTextures(ResourceLocation base) {
        return tbsBottomTextures(suffixedTexture(base, "none"), suffixedTexture(base, "cornerless"), suffixedTexture(base, "vertical"), suffixedTexture(base, "horizontal"), suffixedTexture(base, "corner"));
    }

    /**
     * Supplies standard_side standalone textures in the listed parameter order.
     */
    public CTMModelBuilder tbsSideTextures(ResourceLocation none, ResourceLocation cornerless, ResourceLocation vertical, ResourceLocation horizontal, ResourceLocation corner) {
        return texture(CTMTextureKeys.STANDARD_SIDE_NONE, none)
                .texture(CTMTextureKeys.STANDARD_SIDE_CORNERLESS, cornerless)
                .texture(CTMTextureKeys.STANDARD_SIDE_VERTICAL, vertical)
                .texture(CTMTextureKeys.STANDARD_SIDE_HORIZONTAL, horizontal)
                .texture(CTMTextureKeys.STANDARD_SIDE_CORNER, corner);
    }

    /**
     * Resolves textures as base path plus _none, _cornerless, _vertical, _horizontal, _corner.
     */
    public CTMModelBuilder tbsSideTextures(ResourceLocation base) {
        return tbsSideTextures(suffixedTexture(base, "none"), suffixedTexture(base, "cornerless"), suffixedTexture(base, "vertical"), suffixedTexture(base, "horizontal"), suffixedTexture(base, "corner"));
    }

    public CTMModelBuilder baseTexture(ResourceLocation texture) {
        return texture(CTMTextureKeys.BASE, texture);
    }

    public CTMModelBuilder particleTexture(ResourceLocation texture) {
        return texture(CTMTextureKeys.PARTICLE, texture);
    }

    /**
     * Resolves the fifteen TBS states using separate top, bottom, and side base paths.
     */
    public CTMModelBuilder tbsTextures(ResourceLocation top, ResourceLocation bottom, ResourceLocation side) {
        return tbsTopTextures(top).tbsBottomTextures(bottom).tbsSideTextures(side);
    }

    /**
     * Resolves base_top_none, base_bottom_none, base_side_none, and the other states.
     */
    public CTMModelBuilder tbsTextures(ResourceLocation base) {
        return tbsTextures(suffixedTexture(base, "top"), suffixedTexture(base, "bottom"), suffixedTexture(base, "side"));
    }

    /**
     * Supplies all fifteen TBS states explicitly, grouped top, bottom, then side.
     */
    public CTMModelBuilder tbsTextures(ResourceLocation topNone, ResourceLocation topCornerless, ResourceLocation topVertical, ResourceLocation topHorizontal, ResourceLocation topCorner, ResourceLocation bottomNone, ResourceLocation bottomCornerless, ResourceLocation bottomVertical, ResourceLocation bottomHorizontal, ResourceLocation bottomCorner, ResourceLocation sideNone, ResourceLocation sideCornerless, ResourceLocation sideVertical, ResourceLocation sideHorizontal, ResourceLocation sideCorner) {
        return tbsTopTextures(topNone, topCornerless, topVertical, topHorizontal, topCorner)
                .tbsBottomTextures(bottomNone, bottomCornerless, bottomVertical, bottomHorizontal, bottomCorner)
                .tbsSideTextures(sideNone, sideCornerless, sideVertical, sideHorizontal, sideCorner);
    }

    private static ResourceLocation suffixedTexture(ResourceLocation base, String suffix) {
        return ResourceLocation.fromNamespaceAndPath(base.getNamespace(), base.getPath() + "_" + suffix);
    }

    /**
     * Supplies the full-face texture used by obscured regular EDGES faces.
     */
    public CTMModelBuilder obscuredTexture(ResourceLocation texture) {
        return texture(CTMTextureKeys.OVERLAY_OBSCURED, texture);
    }

    /**
     * Supplies all sixteen EDGES_FULL output states in the existing lookup order.
     */
    public CTMModelBuilder edgesFullTextures(ResourceLocation none, ResourceLocation diagonalsTopLeftBottomRight, ResourceLocation diagonalsTopRightBottomLeft, ResourceLocation topLeft, ResourceLocation cornerBottomRight, ResourceLocation bottom, ResourceLocation cornerBottomLeft, ResourceLocation topRight, ResourceLocation right, ResourceLocation all, ResourceLocation left, ResourceLocation rightBottom, ResourceLocation cornerTopRight, ResourceLocation top, ResourceLocation cornerTopLeft, ResourceLocation bottomLeft) {
        return texture(CTMTextureKeys.EDGES_NONE, none)
                .texture(CTMTextureKeys.EDGES_DIAGONALS_TOP_LEFT_BOTTOM_RIGHT, diagonalsTopLeftBottomRight)
                .texture(CTMTextureKeys.EDGES_DIAGONALS_TOP_RIGHT_BOTTOM_LEFT, diagonalsTopRightBottomLeft)
                .texture(CTMTextureKeys.EDGES_TOP_LEFT, topLeft)
                .texture(CTMTextureKeys.EDGES_CORNER_BOTTOM_RIGHT, cornerBottomRight)
                .texture(CTMTextureKeys.EDGES_BOTTOM, bottom)
                .texture(CTMTextureKeys.EDGES_CORNER_BOTTOM_LEFT, cornerBottomLeft)
                .texture(CTMTextureKeys.EDGES_TOP_RIGHT, topRight)
                .texture(CTMTextureKeys.EDGES_RIGHT, right)
                .texture(CTMTextureKeys.EDGES_ALL, all)
                .texture(CTMTextureKeys.EDGES_LEFT, left)
                .texture(CTMTextureKeys.EDGES_RIGHT_BOTTOM, rightBottom)
                .texture(CTMTextureKeys.EDGES_CORNER_TOP_RIGHT, cornerTopRight)
                .texture(CTMTextureKeys.EDGES_TOP, top)
                .texture(CTMTextureKeys.EDGES_CORNER_TOP_LEFT, cornerTopLeft)
                .texture(CTMTextureKeys.EDGES_BOTTOM_LEFT, bottomLeft);
    }

    /**
     * Resolves each edge state as base path + "_" + its semantic suffix.
     * For example, block/metal_edges becomes block/metal_edges_corner_top_left.
     */
    public CTMModelBuilder edgesFullTextures(ResourceLocation base) {
        for (var logic : CTMLogicEdges.values()) {
            var slot = logic.getTextureSlot();
            texture(slot, ResourceLocation.fromNamespaceAndPath(base.getNamespace(), base.getPath() + "_" + slot.substring("edges_".length())));
        }
        return this;
    }

    public @NotNull CTMModelBuilder with(@NotNull VariantMutator variantMutator) {
        var result = copy();
        var transformed = variantMutator.apply(new CTMModelVariant(modelLocation, modelState));
        result.modelLocation = transformed.modelLocation();
        result.modelState = transformed.modelState();
        return result;
    }

    public @NotNull CTMModelBuilder with(@NotNull UnbakedMutator unbakedMutator) {
        var result = copy();
        result.unbakedMutators.add(unbakedMutator);
        return result;
    }

    private CTMModelBuilder copy() {
        var result = new CTMModelBuilder(this.block, this.kind);
        result.modelLocation = this.modelLocation;
        result.element = this.element;
        result.connectedFaces.addAll(this.connectedFaces);
        result.renderOverlayOnAllFaces = this.renderOverlayOnAllFaces;
        result.baseTintIndex = this.baseTintIndex;
        result.baseEmissivity = this.baseEmissivity;
        result.tintIndex = this.tintIndex;
        result.emissivity = this.emissivity;
        result.shade = this.shade;
        result.ambientOcclusion = this.ambientOcclusion;
        result.eldritch = this.eldritch;
        result.waterOffset = this.waterOffset;
        result.connectionPredicate = this.connectionPredicate;
        result.overlays.addAll(this.overlays);
        result.textureSlots.putAll(this.textureSlots);
        result.modelState = this.modelState;
        result.unbakedMutators.addAll(this.unbakedMutators);
        return result;
    }

    public @NotNull UnbakedConnectedTextureBlockStateModel toUnbaked() {
        var model = new UnbakedConnectedTextureBlockStateModel(
                modelLocation,
                element,
                connectedFaces,
                renderOverlayOnAllFaces,
                CTMVariant.of(block, kind, waterOffset),
                baseTintIndex,
                baseEmissivity,
                tintIndex,
                emissivity,
                shade,
                ambientOcclusion,
                eldritch,
                connectionPredicate,
                overlays,
                textureSlots,
                modelState
        );
        for (var mutator : unbakedMutators) {
            model = mutator.apply(model);
        }
        return model;
    }
}
