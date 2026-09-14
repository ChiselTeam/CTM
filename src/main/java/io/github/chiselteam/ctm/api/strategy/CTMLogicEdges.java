package io.github.chiselteam.ctm.api.strategy;

import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;

public enum CTMLogicEdges {
    NONE(CTMTextureKeys.EDGES_NONE),
    DIAGONALS_TOP_LEFT_BOTTOM_RIGHT(CTMTextureKeys.EDGES_DIAGONALS_TOP_LEFT_BOTTOM_RIGHT),
    DIAGONALS_TOP_RIGHT_BOTTOM_LEFT(CTMTextureKeys.EDGES_DIAGONALS_TOP_RIGHT_BOTTOM_LEFT),
    TOP_LEFT(CTMTextureKeys.EDGES_TOP_LEFT),
    CORNER_BOTTOM_RIGHT(CTMTextureKeys.EDGES_CORNER_BOTTOM_RIGHT),
    BOTTOM(CTMTextureKeys.EDGES_BOTTOM),
    CORNER_BOTTOM_LEFT(CTMTextureKeys.EDGES_CORNER_BOTTOM_LEFT),
    TOP_RIGHT(CTMTextureKeys.EDGES_TOP_RIGHT),
    RIGHT(CTMTextureKeys.EDGES_RIGHT),
    ALL(CTMTextureKeys.EDGES_ALL),
    LEFT(CTMTextureKeys.EDGES_LEFT),
    RIGHT_BOTTOM(CTMTextureKeys.EDGES_RIGHT_BOTTOM),
    CORNER_TOP_RIGHT(CTMTextureKeys.EDGES_CORNER_TOP_RIGHT),
    TOP(CTMTextureKeys.EDGES_TOP),
    CORNER_TOP_LEFT(CTMTextureKeys.EDGES_CORNER_TOP_LEFT),
    BOTTOM_LEFT(CTMTextureKeys.EDGES_BOTTOM_LEFT);

    private final String textureSlot;

    CTMLogicEdges(String textureSlot) {
        this.textureSlot = textureSlot;
    }

    public String getTextureSlot() {
        return textureSlot;
    }
}
