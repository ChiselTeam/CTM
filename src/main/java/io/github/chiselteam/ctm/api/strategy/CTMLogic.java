package io.github.chiselteam.ctm.api.strategy;

import com.mojang.serialization.Codec;
import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.core.Direction.*;

public enum CTMLogic implements StringRepresentable {
    NONE,
    CORNERLESS,
    VERTICAL,
    HORIZONTAL,
    CORNER;

    public static final Codec<CTMLogic> CODEC = StringRepresentable.fromEnum(CTMLogic::values);

    public static final Direction[][] AXIS_PLANE_DIRECTIONS = {
            {UP, NORTH, DOWN, SOUTH},
            {NORTH, EAST, SOUTH, WEST},
            {UP, EAST, DOWN, WEST},
    };

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
            return vertical ? CORNER : VERTICAL;
        } else {
            return vertical ? HORIZONTAL : NONE;
        }
    }

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase();
    }
}
