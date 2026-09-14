package io.github.chiselteam.ctm.api.datagen;

import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;
import net.minecraft.client.data.models.model.TextureSlot;

public final class CTMTextureSlots {

    public static final TextureSlot BASE = TextureSlot.create(CTMTextureKeys.BASE);
    public static final TextureSlot STANDARD_NONE, STANDARD_CORNERLESS, STANDARD_VERTICAL, STANDARD_HORIZONTAL, STANDARD_CORNER;

    // Legacy
    @Deprecated(forRemoval = true, since = "26.1")
    public static final TextureSlot OVERLAY = TextureSlot.create("overlay_texture");

    @Deprecated(forRemoval = true, since = "26.1")
    public static final TextureSlot OVERLAY_CONNECTED = TextureSlot.create("overlay_connected");

    public static final TextureSlot OVERLAY_OBSCURED = TextureSlot.create(CTMTextureKeys.OVERLAY_OBSCURED);
    public static final TextureSlot OVERLAY_TOP = TextureSlot.create("overlay_top");
    public static final TextureSlot OVERLAY_BOTTOM = TextureSlot.create("overlay_bottom");
    public static final TextureSlot OVERLAY_SIDE = TextureSlot.create("overlay_side");
    /**
     * @deprecated Use HORIZONTAL_* standalone slots. Remove in 27.1.
     */
    @Deprecated(forRemoval = true, since = "26.1")
    public static final TextureSlot OVERLAY_HORIZONTAL = TextureSlot.create("overlay_horizontal");
    /**
     * @deprecated Use VERTICAL_* standalone slots. Remove in 27.1.
     */
    @Deprecated(forRemoval = true, since = "26.1")
    public static final TextureSlot OVERLAY_VERTICAL = TextureSlot.create("overlay_vertical");
    /**
     * @deprecated Use MULTIBLOCK_2X2_* or AR_* standalone slots. Remove in 27.1.
     */
    @Deprecated(forRemoval = true, since = "26.1")
    public static final TextureSlot OVERLAY_2X2 = TextureSlot.create("overlay_2x2");
    /**
     * @deprecated Use MULTIBLOCK_3X3_* standalone slots. Remove in 27.1.
     */
    @Deprecated(forRemoval = true, since = "26.1")
    public static final TextureSlot OVERLAY_3X3 = TextureSlot.create("overlay_3x3");
    /**
     * @deprecated Use MULTIBLOCK_4X4_* standalone slots. Remove in 27.1.
     */
    @Deprecated(forRemoval = true, since = "26.1")
    public static final TextureSlot OVERLAY_4X4 = TextureSlot.create("overlay_4x4");

    public static final TextureSlot EDGES_NONE = TextureSlot.create(CTMTextureKeys.EDGES_NONE);
    public static final TextureSlot EDGES_DIAGONALS_TOP_LEFT_BOTTOM_RIGHT = TextureSlot.create(CTMTextureKeys.EDGES_DIAGONALS_TOP_LEFT_BOTTOM_RIGHT);
    public static final TextureSlot EDGES_DIAGONALS_TOP_RIGHT_BOTTOM_LEFT = TextureSlot.create(CTMTextureKeys.EDGES_DIAGONALS_TOP_RIGHT_BOTTOM_LEFT);
    public static final TextureSlot EDGES_TOP_LEFT = TextureSlot.create(CTMTextureKeys.EDGES_TOP_LEFT);
    public static final TextureSlot EDGES_CORNER_BOTTOM_RIGHT = TextureSlot.create(CTMTextureKeys.EDGES_CORNER_BOTTOM_RIGHT);
    public static final TextureSlot EDGES_BOTTOM = TextureSlot.create(CTMTextureKeys.EDGES_BOTTOM);
    public static final TextureSlot EDGES_CORNER_BOTTOM_LEFT = TextureSlot.create(CTMTextureKeys.EDGES_CORNER_BOTTOM_LEFT);
    public static final TextureSlot EDGES_TOP_RIGHT = TextureSlot.create(CTMTextureKeys.EDGES_TOP_RIGHT);
    public static final TextureSlot EDGES_RIGHT = TextureSlot.create(CTMTextureKeys.EDGES_RIGHT);
    public static final TextureSlot EDGES_ALL = TextureSlot.create(CTMTextureKeys.EDGES_ALL);
    public static final TextureSlot EDGES_LEFT = TextureSlot.create(CTMTextureKeys.EDGES_LEFT);
    public static final TextureSlot EDGES_RIGHT_BOTTOM = TextureSlot.create(CTMTextureKeys.EDGES_RIGHT_BOTTOM);
    public static final TextureSlot EDGES_CORNER_TOP_RIGHT = TextureSlot.create(CTMTextureKeys.EDGES_CORNER_TOP_RIGHT);
    public static final TextureSlot EDGES_TOP = TextureSlot.create(CTMTextureKeys.EDGES_TOP);
    public static final TextureSlot EDGES_CORNER_TOP_LEFT = TextureSlot.create(CTMTextureKeys.EDGES_CORNER_TOP_LEFT);
    public static final TextureSlot EDGES_BOTTOM_LEFT = TextureSlot.create(CTMTextureKeys.EDGES_BOTTOM_LEFT);

    public static final TextureSlot HORIZONTAL_NONE = TextureSlot.create(CTMTextureKeys.HORIZONTAL_NONE);
    public static final TextureSlot HORIZONTAL_BOTH = TextureSlot.create(CTMTextureKeys.HORIZONTAL_BOTH);
    public static final TextureSlot HORIZONTAL_LEFT = TextureSlot.create(CTMTextureKeys.HORIZONTAL_LEFT);
    public static final TextureSlot HORIZONTAL_RIGHT = TextureSlot.create(CTMTextureKeys.HORIZONTAL_RIGHT);
    public static final TextureSlot VERTICAL_NONE = TextureSlot.create(CTMTextureKeys.VERTICAL_NONE);
    public static final TextureSlot VERTICAL_BOTH = TextureSlot.create(CTMTextureKeys.VERTICAL_BOTH);
    public static final TextureSlot VERTICAL_TOP = TextureSlot.create(CTMTextureKeys.VERTICAL_TOP);
    public static final TextureSlot VERTICAL_BOTTOM = TextureSlot.create(CTMTextureKeys.VERTICAL_BOTTOM);
    public static final TextureSlot MULTIBLOCK_2X2_TOP_LEFT = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_2X2_TOP_LEFT);
    public static final TextureSlot MULTIBLOCK_2X2_TOP_RIGHT = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_2X2_TOP_RIGHT);
    public static final TextureSlot MULTIBLOCK_2X2_BOTTOM_LEFT = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_2X2_BOTTOM_LEFT);
    public static final TextureSlot MULTIBLOCK_2X2_BOTTOM_RIGHT = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_2X2_BOTTOM_RIGHT);
    public static final TextureSlot MULTIBLOCK_3X3_TOP_LEFT = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_3X3_TOP_LEFT);
    public static final TextureSlot MULTIBLOCK_3X3_TOP_CENTER = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_3X3_TOP_CENTER);
    public static final TextureSlot MULTIBLOCK_3X3_TOP_RIGHT = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_3X3_TOP_RIGHT);
    public static final TextureSlot MULTIBLOCK_3X3_CENTER_LEFT = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_3X3_CENTER_LEFT);
    public static final TextureSlot MULTIBLOCK_3X3_CENTER = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_3X3_CENTER);
    public static final TextureSlot MULTIBLOCK_3X3_CENTER_RIGHT = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_3X3_CENTER_RIGHT);
    public static final TextureSlot MULTIBLOCK_3X3_BOTTOM_LEFT = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_3X3_BOTTOM_LEFT);
    public static final TextureSlot MULTIBLOCK_3X3_BOTTOM_CENTER = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_3X3_BOTTOM_CENTER);
    public static final TextureSlot MULTIBLOCK_3X3_BOTTOM_RIGHT = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_3X3_BOTTOM_RIGHT);
    public static final TextureSlot MULTIBLOCK_4X4_ROW_0_COLUMN_0 = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_4X4_ROW_0_COLUMN_0);
    public static final TextureSlot MULTIBLOCK_4X4_ROW_0_COLUMN_1 = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_4X4_ROW_0_COLUMN_1);
    public static final TextureSlot MULTIBLOCK_4X4_ROW_0_COLUMN_2 = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_4X4_ROW_0_COLUMN_2);
    public static final TextureSlot MULTIBLOCK_4X4_ROW_0_COLUMN_3 = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_4X4_ROW_0_COLUMN_3);
    public static final TextureSlot MULTIBLOCK_4X4_ROW_1_COLUMN_0 = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_4X4_ROW_1_COLUMN_0);
    public static final TextureSlot MULTIBLOCK_4X4_ROW_1_COLUMN_1 = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_4X4_ROW_1_COLUMN_1);
    public static final TextureSlot MULTIBLOCK_4X4_ROW_1_COLUMN_2 = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_4X4_ROW_1_COLUMN_2);
    public static final TextureSlot MULTIBLOCK_4X4_ROW_1_COLUMN_3 = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_4X4_ROW_1_COLUMN_3);
    public static final TextureSlot MULTIBLOCK_4X4_ROW_2_COLUMN_0 = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_4X4_ROW_2_COLUMN_0);
    public static final TextureSlot MULTIBLOCK_4X4_ROW_2_COLUMN_1 = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_4X4_ROW_2_COLUMN_1);
    public static final TextureSlot MULTIBLOCK_4X4_ROW_2_COLUMN_2 = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_4X4_ROW_2_COLUMN_2);
    public static final TextureSlot MULTIBLOCK_4X4_ROW_2_COLUMN_3 = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_4X4_ROW_2_COLUMN_3);
    public static final TextureSlot MULTIBLOCK_4X4_ROW_3_COLUMN_0 = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_4X4_ROW_3_COLUMN_0);
    public static final TextureSlot MULTIBLOCK_4X4_ROW_3_COLUMN_1 = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_4X4_ROW_3_COLUMN_1);
    public static final TextureSlot MULTIBLOCK_4X4_ROW_3_COLUMN_2 = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_4X4_ROW_3_COLUMN_2);
    public static final TextureSlot MULTIBLOCK_4X4_ROW_3_COLUMN_3 = TextureSlot.create(CTMTextureKeys.MULTIBLOCK_4X4_ROW_3_COLUMN_3);
    public static final TextureSlot AR_1 = TextureSlot.create(CTMTextureKeys.AR_1);
    public static final TextureSlot AR_2 = TextureSlot.create(CTMTextureKeys.AR_2);
    public static final TextureSlot AR_3 = TextureSlot.create(CTMTextureKeys.AR_3);
    public static final TextureSlot AR_4 = TextureSlot.create(CTMTextureKeys.AR_4);
    public static final TextureSlot STANDARD_TOP_NONE = TextureSlot.create(CTMTextureKeys.STANDARD_TOP_NONE);
    public static final TextureSlot STANDARD_TOP_CORNERLESS = TextureSlot.create(CTMTextureKeys.STANDARD_TOP_CORNERLESS);
    public static final TextureSlot STANDARD_TOP_VERTICAL = TextureSlot.create(CTMTextureKeys.STANDARD_TOP_VERTICAL);
    public static final TextureSlot STANDARD_TOP_HORIZONTAL = TextureSlot.create(CTMTextureKeys.STANDARD_TOP_HORIZONTAL);
    public static final TextureSlot STANDARD_TOP_CORNER = TextureSlot.create(CTMTextureKeys.STANDARD_TOP_CORNER);
    public static final TextureSlot STANDARD_BOTTOM_NONE = TextureSlot.create(CTMTextureKeys.STANDARD_BOTTOM_NONE);
    public static final TextureSlot STANDARD_BOTTOM_CORNERLESS = TextureSlot.create(CTMTextureKeys.STANDARD_BOTTOM_CORNERLESS);
    public static final TextureSlot STANDARD_BOTTOM_VERTICAL = TextureSlot.create(CTMTextureKeys.STANDARD_BOTTOM_VERTICAL);
    public static final TextureSlot STANDARD_BOTTOM_HORIZONTAL = TextureSlot.create(CTMTextureKeys.STANDARD_BOTTOM_HORIZONTAL);
    public static final TextureSlot STANDARD_BOTTOM_CORNER = TextureSlot.create(CTMTextureKeys.STANDARD_BOTTOM_CORNER);
    public static final TextureSlot STANDARD_SIDE_NONE = TextureSlot.create(CTMTextureKeys.STANDARD_SIDE_NONE);
    public static final TextureSlot STANDARD_SIDE_CORNERLESS = TextureSlot.create(CTMTextureKeys.STANDARD_SIDE_CORNERLESS);
    public static final TextureSlot STANDARD_SIDE_VERTICAL = TextureSlot.create(CTMTextureKeys.STANDARD_SIDE_VERTICAL);
    public static final TextureSlot STANDARD_SIDE_HORIZONTAL = TextureSlot.create(CTMTextureKeys.STANDARD_SIDE_HORIZONTAL);
    public static final TextureSlot STANDARD_SIDE_CORNER = TextureSlot.create(CTMTextureKeys.STANDARD_SIDE_CORNER);

    public static final TextureSlot PARTICLE = TextureSlot.PARTICLE;

    private CTMTextureSlots() {}

    private static TextureSlot create(String key) {
        return TextureSlot.create(key);
    }

    static {
        STANDARD_NONE = create(CTMTextureKeys.STANDARD_NONE);
        STANDARD_CORNERLESS = create(CTMTextureKeys.STANDARD_CORNERLESS);
        STANDARD_VERTICAL = create(CTMTextureKeys.STANDARD_VERTICAL);
        STANDARD_HORIZONTAL = create(CTMTextureKeys.STANDARD_HORIZONTAL);
        STANDARD_CORNER = create(CTMTextureKeys.STANDARD_CORNER);
    }
}
