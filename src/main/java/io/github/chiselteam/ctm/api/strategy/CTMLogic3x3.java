package io.github.chiselteam.ctm.api.strategy;

import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public enum CTMLogic3x3 {
    TOP_LEFT, TOP_CENTER, TOP_RIGHT,
    MID_LEFT, MID_CENTER, MID_RIGHT,
    BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT;

    public String getTextureSlot() {
        return switch (this) {
            case TOP_LEFT -> CTMTextureKeys.MULTIBLOCK_3X3_TOP_LEFT;
            case TOP_CENTER -> CTMTextureKeys.MULTIBLOCK_3X3_TOP_CENTER;
            case TOP_RIGHT -> CTMTextureKeys.MULTIBLOCK_3X3_TOP_RIGHT;
            case MID_LEFT -> CTMTextureKeys.MULTIBLOCK_3X3_CENTER_LEFT;
            case MID_CENTER -> CTMTextureKeys.MULTIBLOCK_3X3_CENTER;
            case MID_RIGHT -> CTMTextureKeys.MULTIBLOCK_3X3_CENTER_RIGHT;
            case BOTTOM_LEFT -> CTMTextureKeys.MULTIBLOCK_3X3_BOTTOM_LEFT;
            case BOTTOM_CENTER -> CTMTextureKeys.MULTIBLOCK_3X3_BOTTOM_CENTER;
            case BOTTOM_RIGHT -> CTMTextureKeys.MULTIBLOCK_3X3_BOTTOM_RIGHT;
        };
    }

    public static CTMLogic3x3 get(BlockPos pos, Direction face) {
        int x = pos.getX() % 3;
        int y = pos.getY() % 3;
        int z = pos.getZ() % 3;

        if (x < 0) x += 3;
        if (y < 0) y += 3;
        if (z < 0) z += 3;

        int col, row;
        switch (face) {
            case UP, DOWN -> {
                col = x;
                row = z;
            }
            case NORTH, SOUTH -> {
                col = x;
                row = 2 - y;
            }
            case EAST, WEST -> {
                col = z;
                row = 2 - y;
            }
            default -> {
                col = 0;
                row = 0;
            }
        }
        return values()[row * 3 + col];
    }
}
