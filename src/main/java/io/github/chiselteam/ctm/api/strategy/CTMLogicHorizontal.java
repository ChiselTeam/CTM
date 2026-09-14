package io.github.chiselteam.ctm.api.strategy;

import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;
import net.minecraft.client.resources.model.cuboid.CuboidFace;

public enum CTMLogicHorizontal {
    NONE(0, 0), BOTH(16, 0),
    RIGHT(0, 16), LEFT(16, 16);

    @Deprecated(forRemoval = true, since = "26.1")
    private final int u, v;

    CTMLogicHorizontal(int u, int v) {
        this.u = u;
        this.v = v;
    }

    public String getTextureSlot() {
        return switch (this) {
            case NONE -> CTMTextureKeys.HORIZONTAL_NONE;
            case BOTH -> CTMTextureKeys.HORIZONTAL_BOTH;
            case RIGHT -> CTMTextureKeys.HORIZONTAL_RIGHT;
            case LEFT -> CTMTextureKeys.HORIZONTAL_LEFT;
        };
    }

    @Deprecated(forRemoval = true, since = "26.1")
    public CuboidFace.UVs remapUVs(CuboidFace.UVs uvs) {
        float minU = (u + uvs.minU()) / 2.0f;
        float minV = (v + uvs.minV()) / 2.0f;
        float maxU = (u + uvs.maxU()) / 2.0f;
        float maxV = (v + uvs.maxV()) / 2.0f;
        return new CuboidFace.UVs(minU, minV, maxU, maxV);
    }

    public static CTMLogicHorizontal get(boolean left, boolean right) {
        if (left && right) return BOTH;
        if (left) return RIGHT;
        if (right) return LEFT;
        return NONE;
    }
}
