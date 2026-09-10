package io.github.chiselteam.ctm.api.datagen;

import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;
import net.minecraft.client.data.models.model.TextureSlot;

public final class CTMTextureSlots {

    public static final TextureSlot BASE = TextureSlot.create("base_texture");
    public static final TextureSlot STANDARD_NONE, STANDARD_CORNERLESS, STANDARD_VERTICAL, STANDARD_HORIZONTAL, STANDARD_CORNER;

    // Legacy
    @Deprecated(forRemoval = true, since = "26.1")
    public static final TextureSlot OVERLAY = TextureSlot.create("overlay_texture");

    @Deprecated(forRemoval = true, since = "26.1")
    public static final TextureSlot OVERLAY_CONNECTED = TextureSlot.create("overlay_connected");

    public static final TextureSlot OVERLAY_OBSCURED = TextureSlot.create("overlay_obscured");
    public static final TextureSlot OVERLAY_TOP = TextureSlot.create("overlay_top");
    public static final TextureSlot OVERLAY_BOTTOM = TextureSlot.create("overlay_bottom");
    public static final TextureSlot OVERLAY_SIDE = TextureSlot.create("overlay_side");
    public static final TextureSlot OVERLAY_HORIZONTAL = TextureSlot.create("overlay_horizontal");
    public static final TextureSlot OVERLAY_VERTICAL = TextureSlot.create("overlay_vertical");
    public static final TextureSlot OVERLAY_2X2 = TextureSlot.create("overlay_2x2");
    public static final TextureSlot OVERLAY_3X3 = TextureSlot.create("overlay_3x3");
    public static final TextureSlot OVERLAY_4X4 = TextureSlot.create("overlay_4x4");

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
