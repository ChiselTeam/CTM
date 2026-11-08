package io.github.chiselteam.ctm.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class CTMOffsetClientHandlerTest {
    @Test void emptyReplacementInvalidatesRemovedPositionsOnlyInItsChunk() {
        var chunk = new ChunkPos(10, 10);
        var otherChunk = new ChunkPos(11, 10);
        var pos = new BlockPos(160, 32, 160);
        var other = new BlockPos(176, 32, 160);
        var offset = new BlockPos(1, 0, 0);
        CTMOffsetClientHandler.updateBlockOffsets(chunk, Map.of(pos, offset), true, ignored -> null);
        CTMOffsetClientHandler.updateBlockOffsets(otherChunk, Map.of(other, offset), true, ignored -> null);
        assertEquals(Set.of(pos), CTMOffsetClientHandler.updateBlockOffsets(chunk, Map.of(), true, ignored -> null));
        assertTrue(CTMOffsetClientHandler.updateBlockOffsets(chunk, Map.of(), true, ignored -> null).isEmpty());
        assertEquals(Set.of(other), CTMOffsetClientHandler.updateBlockOffsets(otherChunk, Map.of(), true, ignored -> null));
    }
}
