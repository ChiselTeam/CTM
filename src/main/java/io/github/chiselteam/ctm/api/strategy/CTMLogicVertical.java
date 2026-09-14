package io.github.chiselteam.ctm.api.strategy;

import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;
import net.minecraft.client.resources.model.cuboid.CuboidFace;

public enum CTMLogicVertical {
    NONE(0, 0), BOTH(0, 16),
    TOP(16, 0), BOTTOM(16, 16);

    @Deprecated(forRemoval = true, since = "26.1")
    private final int u, v;

    CTMLogicVertical(int u, int v) {
        this.u = u;
        this.v = v;
    }

    public String getTextureSlot() {
        return switch (this) {
            case NONE -> CTMTextureKeys.VERTICAL_NONE;
            case BOTH -> CTMTextureKeys.VERTICAL_BOTH;
            case TOP -> CTMTextureKeys.VERTICAL_TOP;
            case BOTTOM -> CTMTextureKeys.VERTICAL_BOTTOM;
        };
    }

    @Deprecated(forRemoval = true, since = "26.1")
    public CuboidFace.UVs remapUVs(CuboidFace.UVs uvs) {
        float minU = (u + uvs.minU()) / 2.0F;
        float minV = (v + uvs.minV()) / 2.0F;
        float maxU = (u + uvs.maxU()) / 2.0F;
        float maxV = (v + uvs.maxV()) / 2.0F;
        return new CuboidFace.UVs(minU, minV, maxU, maxV);
    }

    public static CTMLogicVertical get(boolean top, boolean bottom) {
        if (top && bottom) return BOTH;
        if (top) return BOTTOM;
        if (bottom) return TOP;
        return NONE;
    }
}
