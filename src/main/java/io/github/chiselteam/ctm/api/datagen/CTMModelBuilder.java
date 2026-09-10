package io.github.chiselteam.ctm.api.datagen;

import com.mojang.datafixers.util.Pair;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.api.strategy.CTMKind;
import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;
import io.github.chiselteam.ctm.client.unbaked.CTMModelCodecs;
import io.github.chiselteam.ctm.client.unbaked.UnbakedConnectedTextureBlockStateModel;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.blockstate.CustomBlockStateModelBuilder;
import net.neoforged.neoforge.client.model.generators.blockstate.UnbakedMutator;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

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
public class CTMModelBuilder extends CustomBlockStateModelBuilder {

    private final Block block;
    private final CTMKind kind;
    private Identifier modelLocation;
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
    private final Map<String, Identifier> textureSlots = new HashMap<>();
    private Variant.SimpleModelState modelState = Variant.SimpleModelState.DEFAULT;
    private final List<UnbakedMutator> unbakedMutators = new ArrayList<>();

    protected CTMModelBuilder(Block block, CTMKind kind) {
        this.block = block;
        this.kind = kind;
    }

    /**
     * Creates a builder for any CTM kind.
     */
    public static CTMModelBuilder of(Block block, CTMKind kind, Identifier modelLocation) {
        return new CTMModelBuilder(block, kind).modelLocation(modelLocation);
    }

    /** Composes CTM builders in bottom-to-top draw order. */
    public static LayeredCTMModelBuilder layered(CTMModelBuilder bottom, CTMModelBuilder... upperLayers) {
        return LayeredCTMModelBuilder.of(bottom, upperLayers);
    }

    /**
     * Creates a builder for a standard 5-bit CTM model.
     */
    public static CTMModelBuilder standard(Block block, Identifier modelLocation) {
        return new CTMModelBuilder(block, CTMKind.STANDARD).modelLocation(modelLocation);
    }

    /**
     * Creates a builder for a top/bottom/side (TBS) CTM model.
     */
    public static CTMModelBuilder tbs(Block block, Identifier modelLocation) {
        return new CTMModelBuilder(block, CTMKind.TBS).modelLocation(modelLocation);
    }

    /**
     * Creates a builder for a horizontal CTM model.
     */
    public static CTMModelBuilder horizontal(Block block, Identifier modelLocation) {
        return new CTMModelBuilder(block, CTMKind.CTMH).modelLocation(modelLocation);
    }

    /**
     * Creates a builder for a vertical CTM model.
     */
    public static CTMModelBuilder vertical(Block block, Identifier modelLocation) {
        return new CTMModelBuilder(block, CTMKind.CTMV).modelLocation(modelLocation);
    }

    /**
     * Creates a builder for a bookshelf-like CTM model.
     */
    public static CTMModelBuilder bookshelf(Block block, Identifier modelLocation) {
        return new CTMModelBuilder(block, CTMKind.BOOKSHELF).modelLocation(modelLocation);
    }

    /**
     * Creates a builder for a 2x2 multiblock CTM model.
     */
    public static CTMModelBuilder multiblock2x2(Block block, Identifier modelLocation) {
        return new CTMModelBuilder(block, CTMKind.MULTIBLOCK_2X2).modelLocation(modelLocation);
    }

    /**
     * Creates a builder for a 3x3 multiblock CTM model.
     */
    public static CTMModelBuilder multiblock3x3(Block block, Identifier modelLocation) {
        return new CTMModelBuilder(block, CTMKind.MULTIBLOCK_3X3).modelLocation(modelLocation);
    }

    /**
     * Creates a builder for a 4x4 multiblock CTM model.
     */
    public static CTMModelBuilder multiblock4x4(Block block, Identifier modelLocation) {
        return new CTMModelBuilder(block, CTMKind.MULTIBLOCK_4X4).modelLocation(modelLocation);
    }

    /**
     * Creates a builder for an anti-repeat (AR) CTM model.
     */
    public static CTMModelBuilder ar(Block block, Identifier modelLocation) {
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
            Identifier modelLocation
    ) {
        return new CTMModelBuilder(
                block,
                CTMKind.EDGES
        ).modelLocation(modelLocation);
    }

    /**
     * Creates a builder for a full-face-edge CTM model.
     *
     * <p>EDGES_FULL chooses one cell from a 4x4 texture atlas and maps it
     * across the complete block face.
     */
    public static CTMModelBuilder edgesFull(
            Block block,
            Identifier modelLocation
    ) {
        return new CTMModelBuilder(
                block,
                CTMKind.EDGES_FULL
        ).modelLocation(modelLocation);
    }

    public CTMModelBuilder modelLocation(Identifier modelLocation) {
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

    public CTMModelBuilder texture(String slot, Identifier texture) {
        this.textureSlots.put(slot, texture);
        return this;
    }

    public CTMModelBuilder texture(TextureSlot slot, Identifier texture) {
        return texture(slot.getId(), texture);
    };

    public CTMModelBuilder standardTextures(Identifier none, Identifier cornerless, Identifier vertical, Identifier horizontal, Identifier corner) {
        return texture(CTMTextureKeys.STANDARD_NONE, none)
                .texture(CTMTextureKeys.STANDARD_CORNERLESS, cornerless)
                .texture(CTMTextureKeys.STANDARD_VERTICAL, vertical)
                .texture(CTMTextureKeys.STANDARD_HORIZONTAL, horizontal)
                .texture(CTMTextureKeys.STANDARD_CORNER, corner);
    }

    public CTMModelBuilder standardTextures(Identifier base) {
        var namespace = base.getNamespace();
        var path = base.getPath();

        return standardTextures(
                Identifier.fromNamespaceAndPath(namespace, "%s_none".formatted(path)),
                Identifier.fromNamespaceAndPath(namespace, "%s_cornerless".formatted(path)),
                Identifier.fromNamespaceAndPath(namespace, "%s_vertical".formatted(path)),
                Identifier.fromNamespaceAndPath(namespace, "%s_horizontal".formatted(path)),
                Identifier.fromNamespaceAndPath(namespace, "%s_corner".formatted(path))
        );
    }

    @Override
    public @NonNull CTMModelBuilder with(@NonNull VariantMutator variantMutator) {
        var result = copy();
        var transformed = variantMutator.apply(new Variant(modelLocation, modelState));
        result.modelLocation = transformed.modelLocation();
        result.modelState = transformed.modelState();
        return result;
    }

    @Override
    public @NonNull CTMModelBuilder with(@NonNull UnbakedMutator unbakedMutator) {
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

    @Override
    public @NonNull UnbakedConnectedTextureBlockStateModel toUnbaked() {
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
