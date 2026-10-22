package io.github.chiselteam.ctm.api.strategy;

import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;

public enum CTMLogicHorizontal {
    NONE, BOTH,
    RIGHT, LEFT;

    public String getTextureSlot() {
        return switch (this) {
            case NONE -> CTMTextureKeys.HORIZONTAL_NONE;
            case BOTH -> CTMTextureKeys.HORIZONTAL_BOTH;
            case RIGHT -> CTMTextureKeys.HORIZONTAL_RIGHT;
            case LEFT -> CTMTextureKeys.HORIZONTAL_LEFT;
        };
    }

    public static CTMLogicHorizontal get(boolean left, boolean right) {
        if (left && right) return BOTH;
        if (left) return RIGHT;
        if (right) return LEFT;
        return NONE;
    }
}
