package io.github.chiselteam.ctm.api.strategy;

import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;

public enum CTMLogicVertical {
    NONE, BOTH,
    TOP, BOTTOM;

    public String getTextureSlot() {
        return switch (this) {
            case NONE -> CTMTextureKeys.VERTICAL_NONE;
            case BOTH -> CTMTextureKeys.VERTICAL_BOTH;
            case TOP -> CTMTextureKeys.VERTICAL_TOP;
            case BOTTOM -> CTMTextureKeys.VERTICAL_BOTTOM;
        };
    }

    public static CTMLogicVertical get(boolean top, boolean bottom) {
        if (top && bottom) return BOTH;
        if (top) return BOTTOM;
        if (bottom) return TOP;
        return NONE;
    }
}
