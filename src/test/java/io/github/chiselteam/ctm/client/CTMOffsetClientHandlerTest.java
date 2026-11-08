package io.github.chiselteam.ctm.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CTMOffsetClientHandlerTest {
    @Test void emptyReplacementInvalidatesRemovedPositionsOnlyInItsChunk() {
        BlockPos removed = new BlockPos(-1, 32, -1);
        BlockPos retained = new BlockPos(0, 32, -1);
        Map<BlockPos, String> offsets = new HashMap<>(Map.of(removed, "old", retained, "keep"));
        List<BlockPos> invalidated = new ArrayList<>();
        CTMOffsetClientHandler.removeChunkOffsets(offsets, new ChunkPos(-1, -1), invalidated::add);
        assertEquals(Map.of(retained, "keep"), offsets);
        assertEquals(List.of(removed), invalidated);
    }
}
