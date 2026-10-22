package io.github.chiselteam.ctm.api.strategy;

import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public enum CTMLogic2x2 {
    TOP_LEFT, TOP_RIGHT,
    BOTTOM_LEFT, BOTTOM_RIGHT;

    public String getTextureSlot() {
        return switch (this) {
            case TOP_LEFT -> CTMTextureKeys.MULTIBLOCK_2X2_TOP_LEFT;
            case TOP_RIGHT -> CTMTextureKeys.MULTIBLOCK_2X2_TOP_RIGHT;
            case BOTTOM_LEFT -> CTMTextureKeys.MULTIBLOCK_2X2_BOTTOM_LEFT;
            case BOTTOM_RIGHT -> CTMTextureKeys.MULTIBLOCK_2X2_BOTTOM_RIGHT;
        };
    }

    public static CTMLogic2x2 get(BlockPos pos, Direction face) {
        int x = pos.getX() % 2;
        int y = pos.getY() % 2;
        int z = pos.getZ() % 2;

        if (x < 0) x += 2;
        if (y < 0) y += 2;
        if (z < 0) z += 2;

        int col, row;
        switch (face) {
            case UP, DOWN -> {
                col = x;
                row = z;
            }
            case NORTH, SOUTH -> {
                col = x;
                row = 1 - y;
            }
            case EAST, WEST -> {
                col = z;
                row = 1 - y;
            }
            default -> {
                col = 0;
                row = 0;
            }
        }
        return values()[row * 2 + col];
    }
}
