package io.github.chiselteam.ctm.api.strategy;

import com.mojang.serialization.Codec;
import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

import static net.minecraft.core.Direction.*;

public enum CTMLogic implements StringRepresentable {
    NONE(0, 0, 0, 16, 16),
    CORNERLESS(1, 0, 0, 8, 8),
    VERTICAL(1, 0, 8, 8, 16),
    HORIZONTAL(1, 8, 0, 16, 8),
    CORNER(1, 8, 8, 16, 16);

    public static final Codec<CTMLogic> CODEC = StringRepresentable.fromEnum(CTMLogic::values);

    @Deprecated(forRemoval = true, since = "26.1")
    private final int texture;

    @Deprecated(forRemoval = true, since = "26.1")
    private final int u0, v0, u1, v1;

    public static final Direction[][] AXIS_PLANE_DIRECTIONS = {
            {UP, NORTH, DOWN, SOUTH},
            {NORTH, EAST, SOUTH, WEST},
            {UP, EAST, DOWN, WEST},
    };

    CTMLogic(int texture, int u0, int v0, int u1, int v1) {
        this.texture = texture;
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
    }

    public String getStandardTextureSlot() {
        return switch(this) {
            case NONE -> CTMTextureKeys.STANDARD_NONE;
            case CORNERLESS -> CTMTextureKeys.STANDARD_CORNERLESS;
            case VERTICAL -> CTMTextureKeys.STANDARD_VERTICAL;
            case HORIZONTAL -> CTMTextureKeys.STANDARD_HORIZONTAL;
            case CORNER -> CTMTextureKeys.STANDARD_CORNER;
        };
    }

    public String getStandardTextureSlot(Direction face) {
        return switch (face) {
            case UP -> switch (this) {
                case NONE -> CTMTextureKeys.STANDARD_TOP_NONE;
                case CORNERLESS -> CTMTextureKeys.STANDARD_TOP_CORNERLESS;
                case VERTICAL -> CTMTextureKeys.STANDARD_TOP_VERTICAL;
                case HORIZONTAL -> CTMTextureKeys.STANDARD_TOP_HORIZONTAL;
                case CORNER -> CTMTextureKeys.STANDARD_TOP_CORNER;
            };
            case DOWN -> switch (this) {
                case NONE -> CTMTextureKeys.STANDARD_BOTTOM_NONE;
                case CORNERLESS -> CTMTextureKeys.STANDARD_BOTTOM_CORNERLESS;
                case VERTICAL -> CTMTextureKeys.STANDARD_BOTTOM_VERTICAL;
                case HORIZONTAL -> CTMTextureKeys.STANDARD_BOTTOM_HORIZONTAL;
                case CORNER -> CTMTextureKeys.STANDARD_BOTTOM_CORNER;
            };
            default -> switch (this) {
                case NONE -> CTMTextureKeys.STANDARD_SIDE_NONE;
                case CORNERLESS -> CTMTextureKeys.STANDARD_SIDE_CORNERLESS;
                case VERTICAL -> CTMTextureKeys.STANDARD_SIDE_VERTICAL;
                case HORIZONTAL -> CTMTextureKeys.STANDARD_SIDE_HORIZONTAL;
                case CORNER -> CTMTextureKeys.STANDARD_SIDE_CORNER;
            };
        };
    }

    public static CTMLogic of(boolean horizontal, boolean vertical, boolean corner) {
        if (corner) {
            return CORNERLESS;
        } else if (horizontal) {
            return vertical ? CORNER : HORIZONTAL;
        } else {
            return vertical ? VERTICAL : NONE;
        }
    }

    @Deprecated(forRemoval = true, since = "26.1")
    public Material.Baked chooseMaterial(Material.Baked[] textures) {
        return textures[texture];
    }

    @Deprecated(forRemoval = true, since = "26.1")
    public CuboidFace.UVs remapUVs(CuboidFace.UVs uvs) {
        return new CuboidFace.UVs(getU(uvs.minU()), getV(uvs.minV()), getU(uvs.maxU()), getV(uvs.maxV()));
    }

    public float getU(float delta) {
        return (float) this.u0 + (float) (this.u1 - this.u0) * (delta / 16.0F);
    }

    public float getV(float delta) {
        return (float) this.v0 + (float) (this.v1 - this.v0) * (delta / 16.0F);
    }

    @Override
    public @NonNull String getSerializedName() {
        return name().toLowerCase();
    }
}
