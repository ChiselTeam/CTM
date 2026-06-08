package io.github.chiselteam.ctm.api.texture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.Map;

public class CTMOffsetDataManager {

    private static final Map<ChunkPos, BlockPos> chunkOffsets = new HashMap<>();

    public static BlockPos getOffset(ChunkPos pos) {
        return chunkOffsets.getOrDefault(pos, BlockPos.ZERO);
    }

    public static void setOffset(ChunkPos pos, BlockPos offset) {
        chunkOffsets.put(pos, offset);
    }
}
