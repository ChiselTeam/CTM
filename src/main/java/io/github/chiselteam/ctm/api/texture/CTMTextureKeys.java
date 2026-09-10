package io.github.chiselteam.ctm.api.texture;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class CTMTextureKeys {

    public static final String BASE = "base_texture";
    public static final String STANDARD_NONE = "standard_none";
    public static final String STANDARD_CORNERLESS = "standard_cornerless";
    public static final String STANDARD_VERTICAL = "standard_vertical";
    public static final String STANDARD_HORIZONTAL = "standard_horizontal";
    public static final String STANDARD_CORNER =  "standard_corner";

    public static final String OVERLAY_OBSCURED = "overlay_obscured";
    public static final String PARTICLE = "particle";

    private CTMTextureKeys() {
    }
}
