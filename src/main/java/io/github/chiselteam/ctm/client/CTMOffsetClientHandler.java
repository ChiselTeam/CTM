package io.github.chiselteam.ctm.client;

import io.github.chiselteam.ctm.api.texture.MultiblockOffsetProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.Map;

public class CTMOffsetClientHandler {

    private static final Map<ChunkPos, BlockPos> chunkOffsets = new HashMap<>();

    static {
        MultiblockOffsetProvider.set(pos -> {
            ChunkPos chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
            BlockPos offset = getOffset(chunkPos);
            return pos.offset(offset);
        });
    }

    public static void setOffset(ChunkPos pos, BlockPos offset) {
        chunkOffsets.put(pos, offset);
    }

    public static BlockPos getOffset(ChunkPos pos) {
        return chunkOffsets.getOrDefault(pos, BlockPos.ZERO);
    }
}
