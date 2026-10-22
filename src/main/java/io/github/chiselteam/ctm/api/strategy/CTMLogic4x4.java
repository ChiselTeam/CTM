package io.github.chiselteam.ctm.api.strategy;

import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public enum CTMLogic4x4 {
    ROW0_COL0, ROW0_COL1, ROW0_COL2, ROW0_COL3,
    ROW1_COL0, ROW1_COL1, ROW1_COL2, ROW1_COL3,
    ROW2_COL0, ROW2_COL1, ROW2_COL2, ROW2_COL3,
    ROW3_COL0, ROW3_COL1, ROW3_COL2, ROW3_COL3;

    public String getTextureSlot() {
        return switch (this) {
            case ROW0_COL0 -> CTMTextureKeys.MULTIBLOCK_4X4_ROW_0_COLUMN_0;
            case ROW0_COL1 -> CTMTextureKeys.MULTIBLOCK_4X4_ROW_0_COLUMN_1;
            case ROW0_COL2 -> CTMTextureKeys.MULTIBLOCK_4X4_ROW_0_COLUMN_2;
            case ROW0_COL3 -> CTMTextureKeys.MULTIBLOCK_4X4_ROW_0_COLUMN_3;
            case ROW1_COL0 -> CTMTextureKeys.MULTIBLOCK_4X4_ROW_1_COLUMN_0;
            case ROW1_COL1 -> CTMTextureKeys.MULTIBLOCK_4X4_ROW_1_COLUMN_1;
            case ROW1_COL2 -> CTMTextureKeys.MULTIBLOCK_4X4_ROW_1_COLUMN_2;
            case ROW1_COL3 -> CTMTextureKeys.MULTIBLOCK_4X4_ROW_1_COLUMN_3;
            case ROW2_COL0 -> CTMTextureKeys.MULTIBLOCK_4X4_ROW_2_COLUMN_0;
            case ROW2_COL1 -> CTMTextureKeys.MULTIBLOCK_4X4_ROW_2_COLUMN_1;
            case ROW2_COL2 -> CTMTextureKeys.MULTIBLOCK_4X4_ROW_2_COLUMN_2;
            case ROW2_COL3 -> CTMTextureKeys.MULTIBLOCK_4X4_ROW_2_COLUMN_3;
            case ROW3_COL0 -> CTMTextureKeys.MULTIBLOCK_4X4_ROW_3_COLUMN_0;
            case ROW3_COL1 -> CTMTextureKeys.MULTIBLOCK_4X4_ROW_3_COLUMN_1;
            case ROW3_COL2 -> CTMTextureKeys.MULTIBLOCK_4X4_ROW_3_COLUMN_2;
            case ROW3_COL3 -> CTMTextureKeys.MULTIBLOCK_4X4_ROW_3_COLUMN_3;
        };
    }

    public static CTMLogic4x4 get(BlockPos pos, Direction face) {
        int x = pos.getX() % 4;
        int y = pos.getY() % 4;
        int z = pos.getZ() % 4;

        if (x < 0) x += 4;
        if (y < 0) y += 4;
        if (z < 0) z += 4;

        int col, row;
        switch (face) {
            case UP, DOWN -> {
                col = x;
                row = z;
            }
            case NORTH, SOUTH -> {
                col = x;
                row = 3 - y;
            }
            case EAST, WEST -> {
                col = z;
                row = 3 - y;
            }
            default -> {
                col = 0;
                row = 0;
            }
        }
        return values()[row * 4 + col];
    }
}
