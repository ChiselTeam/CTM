package io.github.chiselteam.ctm.api.datagen;

import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/**
 * Common {@link ModelTemplate}s used by the CTM library for datagen.
 * These templates reference the custom model loaders registered by the CTM library.
 */
public final class CTMModelTemplates {

    private static final Identifier CTM_LOADER = Identifier.fromNamespaceAndPath("ctm", "connected_texture_model");
    private static final Identifier ELDRITCH_LOADER = Identifier.fromNamespaceAndPath("ctm", "eldritch_model");

    /**
     * Standard CTM template: base texture and connected overlay.
     */
    public static final ModelTemplate STANDARD = template(
            CTM_LOADER,
            TextureSlot.PARTICLE,
            CTMTextureSlots.BASE,
            CTMTextureSlots.STANDARD_NONE,
            CTMTextureSlots.STANDARD_CORNERLESS,
            CTMTextureSlots.STANDARD_VERTICAL,
            CTMTextureSlots.STANDARD_HORIZONTAL,
            CTMTextureSlots.STANDARD_CORNER
    );

    @Deprecated(forRemoval = true, since = "26.1")
    public static final ModelTemplate STANDARD_LEGACY = template(
            CTM_LOADER,
            TextureSlot.PARTICLE,
            CTMTextureSlots.BASE,
            CTMTextureSlots.OVERLAY_CONNECTED
    );

    /**
     * TBS CTM template: base texture and fifteen standalone top/bottom/side states.
     */
    public static final ModelTemplate TBS = template(
            CTM_LOADER, TextureSlot.PARTICLE, CTMTextureSlots.BASE,
            CTMTextureSlots.STANDARD_TOP_NONE,
            CTMTextureSlots.STANDARD_TOP_CORNERLESS,
            CTMTextureSlots.STANDARD_TOP_VERTICAL,
            CTMTextureSlots.STANDARD_TOP_HORIZONTAL,
            CTMTextureSlots.STANDARD_TOP_CORNER,
            CTMTextureSlots.STANDARD_BOTTOM_NONE,
            CTMTextureSlots.STANDARD_BOTTOM_CORNERLESS,
            CTMTextureSlots.STANDARD_BOTTOM_VERTICAL,
            CTMTextureSlots.STANDARD_BOTTOM_HORIZONTAL,
            CTMTextureSlots.STANDARD_BOTTOM_CORNER,
            CTMTextureSlots.STANDARD_SIDE_NONE,
            CTMTextureSlots.STANDARD_SIDE_CORNERLESS,
            CTMTextureSlots.STANDARD_SIDE_VERTICAL,
            CTMTextureSlots.STANDARD_SIDE_HORIZONTAL,
            CTMTextureSlots.STANDARD_SIDE_CORNER
    );

    /**
     * @deprecated Replaced by standalone textures. Remove in 27.1.
     */
    @Deprecated(forRemoval = true, since = "26.1")
    public static final ModelTemplate TBS_LEGACY = template(CTM_LOADER, TextureSlot.PARTICLE, TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE, CTMTextureSlots.OVERLAY_CONNECTED);

    /**
     * Horizontal CTM template: base texture and four standalone horizontal states.
     */
    public static final ModelTemplate HORIZONTAL = template(
            CTM_LOADER, TextureSlot.PARTICLE, CTMTextureSlots.BASE,
            CTMTextureSlots.HORIZONTAL_NONE,
            CTMTextureSlots.HORIZONTAL_BOTH,
            CTMTextureSlots.HORIZONTAL_LEFT,
            CTMTextureSlots.HORIZONTAL_RIGHT
    );

    /**
     * @deprecated Replaced by standalone textures. Remove in 27.1.
     */
    @Deprecated(forRemoval = true, since = "26.1")
    public static final ModelTemplate HORIZONTAL_LEGACY = template(CTM_LOADER, TextureSlot.PARTICLE, CTMTextureSlots.BASE, CTMTextureSlots.OVERLAY_HORIZONTAL);

    /**
     * Vertical CTM template: base texture and four standalone vertical states.
     */
    public static final ModelTemplate VERTICAL = template(
            CTM_LOADER, TextureSlot.PARTICLE, CTMTextureSlots.BASE,
            CTMTextureSlots.VERTICAL_NONE,
            CTMTextureSlots.VERTICAL_BOTH,
            CTMTextureSlots.VERTICAL_TOP,
            CTMTextureSlots.VERTICAL_BOTTOM
    );

    /**
     * @deprecated Replaced by standalone textures. Remove in 27.1.
     */
    @Deprecated(forRemoval = true, since = "26.1")
    public static final ModelTemplate VERTICAL_LEGACY = template(CTM_LOADER, TextureSlot.PARTICLE, CTMTextureSlots.BASE, CTMTextureSlots.OVERLAY_VERTICAL);

    /**
     * Multiblock CTM templates: base texture and standalone cells in row-major order.
     */
    public static final ModelTemplate MULTIBLOCK_2X2 = template(
            CTM_LOADER, TextureSlot.PARTICLE, CTMTextureSlots.BASE,
            CTMTextureSlots.MULTIBLOCK_2X2_TOP_LEFT,
            CTMTextureSlots.MULTIBLOCK_2X2_TOP_RIGHT,
            CTMTextureSlots.MULTIBLOCK_2X2_BOTTOM_LEFT,
            CTMTextureSlots.MULTIBLOCK_2X2_BOTTOM_RIGHT
    );

    /**
     * @deprecated Replaced by standalone textures. Remove in 27.1.
     */
    @Deprecated(forRemoval = true, since = "26.1")
    public static final ModelTemplate MULTIBLOCK_2X2_LEGACY = template(CTM_LOADER, TextureSlot.PARTICLE, CTMTextureSlots.BASE, CTMTextureSlots.OVERLAY_2X2);
    public static final ModelTemplate MULTIBLOCK_3X3 = template(
            CTM_LOADER, TextureSlot.PARTICLE, CTMTextureSlots.BASE,
            CTMTextureSlots.MULTIBLOCK_3X3_TOP_LEFT,
            CTMTextureSlots.MULTIBLOCK_3X3_TOP_CENTER,
            CTMTextureSlots.MULTIBLOCK_3X3_TOP_RIGHT,
            CTMTextureSlots.MULTIBLOCK_3X3_CENTER_LEFT,
            CTMTextureSlots.MULTIBLOCK_3X3_CENTER,
            CTMTextureSlots.MULTIBLOCK_3X3_CENTER_RIGHT,
            CTMTextureSlots.MULTIBLOCK_3X3_BOTTOM_LEFT,
            CTMTextureSlots.MULTIBLOCK_3X3_BOTTOM_CENTER,
            CTMTextureSlots.MULTIBLOCK_3X3_BOTTOM_RIGHT
    );

    /**
     * @deprecated Replaced by standalone textures. Remove in 27.1.
     */
    @Deprecated(forRemoval = true, since = "26.1")
    public static final ModelTemplate MULTIBLOCK_3X3_LEGACY = template(CTM_LOADER, TextureSlot.PARTICLE, CTMTextureSlots.BASE, CTMTextureSlots.OVERLAY_3X3);
    public static final ModelTemplate MULTIBLOCK_4X4 = template(
            CTM_LOADER, TextureSlot.PARTICLE, CTMTextureSlots.BASE,
            CTMTextureSlots.MULTIBLOCK_4X4_ROW_0_COLUMN_0,
            CTMTextureSlots.MULTIBLOCK_4X4_ROW_0_COLUMN_1,
            CTMTextureSlots.MULTIBLOCK_4X4_ROW_0_COLUMN_2,
            CTMTextureSlots.MULTIBLOCK_4X4_ROW_0_COLUMN_3,
            CTMTextureSlots.MULTIBLOCK_4X4_ROW_1_COLUMN_0,
            CTMTextureSlots.MULTIBLOCK_4X4_ROW_1_COLUMN_1,
            CTMTextureSlots.MULTIBLOCK_4X4_ROW_1_COLUMN_2,
            CTMTextureSlots.MULTIBLOCK_4X4_ROW_1_COLUMN_3,
            CTMTextureSlots.MULTIBLOCK_4X4_ROW_2_COLUMN_0,
            CTMTextureSlots.MULTIBLOCK_4X4_ROW_2_COLUMN_1,
            CTMTextureSlots.MULTIBLOCK_4X4_ROW_2_COLUMN_2,
            CTMTextureSlots.MULTIBLOCK_4X4_ROW_2_COLUMN_3,
            CTMTextureSlots.MULTIBLOCK_4X4_ROW_3_COLUMN_0,
            CTMTextureSlots.MULTIBLOCK_4X4_ROW_3_COLUMN_1,
            CTMTextureSlots.MULTIBLOCK_4X4_ROW_3_COLUMN_2,
            CTMTextureSlots.MULTIBLOCK_4X4_ROW_3_COLUMN_3
    );

    /**
     * @deprecated Replaced by standalone textures. Remove in 27.1.
     */
    @Deprecated(forRemoval = true, since = "26.1")
    public static final ModelTemplate MULTIBLOCK_4X4_LEGACY = template(CTM_LOADER, TextureSlot.PARTICLE, CTMTextureSlots.BASE, CTMTextureSlots.OVERLAY_4X4);

    /**
     * Anti-repeat (AR) template: base texture and four standalone variants.
     */
    public static final ModelTemplate AR = template(
            CTM_LOADER, TextureSlot.PARTICLE, CTMTextureSlots.BASE,
            CTMTextureSlots.AR_1,
            CTMTextureSlots.AR_2,
            CTMTextureSlots.AR_3,
            CTMTextureSlots.AR_4
    );

    /**
     * @deprecated Replaced by standalone textures. Remove in 27.1.
     */
    @Deprecated(forRemoval = true, since = "26.1")
    public static final ModelTemplate AR_LEGACY = template(CTM_LOADER, TextureSlot.PARTICLE, CTMTextureSlots.BASE, CTMTextureSlots.OVERLAY);

    public static final ModelTemplate EDGES = template(CTM_LOADER, TextureSlot.PARTICLE, CTMTextureSlots.BASE, CTMTextureSlots.STANDARD_NONE, CTMTextureSlots.STANDARD_CORNERLESS, CTMTextureSlots.STANDARD_VERTICAL, CTMTextureSlots.STANDARD_HORIZONTAL, CTMTextureSlots.STANDARD_CORNER, CTMTextureSlots.OVERLAY_OBSCURED);

    public static final ModelTemplate EDGES_FULL = template(
            CTM_LOADER, TextureSlot.PARTICLE, CTMTextureSlots.BASE,
            CTMTextureSlots.EDGES_NONE,
            CTMTextureSlots.EDGES_DIAGONALS_TOP_LEFT_BOTTOM_RIGHT,
            CTMTextureSlots.EDGES_DIAGONALS_TOP_RIGHT_BOTTOM_LEFT,
            CTMTextureSlots.EDGES_TOP_LEFT,
            CTMTextureSlots.EDGES_CORNER_BOTTOM_RIGHT,
            CTMTextureSlots.EDGES_BOTTOM,
            CTMTextureSlots.EDGES_CORNER_BOTTOM_LEFT,
            CTMTextureSlots.EDGES_TOP_RIGHT,
            CTMTextureSlots.EDGES_RIGHT,
            CTMTextureSlots.EDGES_ALL,
            CTMTextureSlots.EDGES_LEFT,
            CTMTextureSlots.EDGES_RIGHT_BOTTOM,
            CTMTextureSlots.EDGES_CORNER_TOP_RIGHT,
            CTMTextureSlots.EDGES_TOP,
            CTMTextureSlots.EDGES_CORNER_TOP_LEFT,
            CTMTextureSlots.EDGES_BOTTOM_LEFT
    );

    /**
     * @deprecated Replaced by standalone Standard-state textures. Remove in 27.1.
     */
    @Deprecated(forRemoval = true, since = "26.1")
    public static final ModelTemplate EDGES_LEGACY = template(CTM_LOADER, CTMTextureSlots.BASE, CTMTextureSlots.OVERLAY, CTMTextureSlots.OVERLAY_CONNECTED, CTMTextureSlots.OVERLAY_OBSCURED, TextureSlot.PARTICLE);

    /**
     * @deprecated Replaced by standalone edge-state textures. Remove in 27.1.
     */
    @Deprecated(forRemoval = true, since = "26.1")
    public static final ModelTemplate EDGES_FULL_LEGACY = template(CTM_LOADER, CTMTextureSlots.BASE, CTMTextureSlots.OVERLAY, CTMTextureSlots.OVERLAY_CONNECTED, TextureSlot.PARTICLE);

    private static ModelTemplate template(Identifier loader, TextureSlot... requiredSlots) {
        return new ModelTemplate(Optional.empty(), Optional.of(loader.toString()), requiredSlots);
    }

    private CTMModelTemplates() {
    }
}
