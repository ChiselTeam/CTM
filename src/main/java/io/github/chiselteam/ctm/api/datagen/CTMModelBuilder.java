package io.github.chiselteam.ctm.api.datagen;

import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.api.strategy.CTMKind;
import io.github.chiselteam.ctm.client.unbaked.CTMModelCodecs;
import io.github.chiselteam.ctm.client.unbaked.UnbakedConnectedTextureBlockStateModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.blockstate.CustomBlockStateModelBuilder;
import net.neoforged.neoforge.client.model.generators.blockstate.UnbakedMutator;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private boolean eldritch = false;
    private boolean waterOffset = false;
    private CTMBlockPredicate connectionPredicate = CTMBlockPredicate.sameBlock();
    private final List<CTMModelCodecs.UnbakedOverlayRule> overlays = new ArrayList<>();
    private final Map<String, Identifier> textureSlots = new HashMap<>();

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

    @Override
    public @NonNull CTMModelBuilder with(@NonNull VariantMutator variantMutator) {
        return this;
    }

    @Override
    public @NonNull CTMModelBuilder with(@NonNull UnbakedMutator unbakedMutator) {
        CTMModelBuilder result = new CTMModelBuilder(this.block, this.kind);
        result.modelLocation = this.modelLocation;
        result.element = this.element;
        result.connectedFaces.addAll(this.connectedFaces);
        result.renderOverlayOnAllFaces = this.renderOverlayOnAllFaces;
        result.baseTintIndex = this.baseTintIndex;
        result.baseEmissivity = this.baseEmissivity;
        result.tintIndex = this.tintIndex;
        result.emissivity = this.emissivity;
        result.eldritch = this.eldritch;
        result.waterOffset = this.waterOffset;
        result.connectionPredicate = this.connectionPredicate;
        result.overlays.addAll(this.overlays);
        result.textureSlots.putAll(this.textureSlots);
        return result;
    }

    @Override
    public @NonNull UnbakedConnectedTextureBlockStateModel toUnbaked() {
        return new UnbakedConnectedTextureBlockStateModel(
                modelLocation,
                element,
                connectedFaces,
                renderOverlayOnAllFaces,
                CTMVariant.of(block, kind, waterOffset),
                baseTintIndex,
                baseEmissivity,
                tintIndex,
                emissivity,
                eldritch,
                connectionPredicate,
                overlays,
                textureSlots
        );
    }
}
