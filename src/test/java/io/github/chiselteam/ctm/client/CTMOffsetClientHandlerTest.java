package io.github.chiselteam.ctm.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class CTMOffsetClientHandlerTest {
    @Test void emptyReplacementInvalidatesRemovedPositionsAndKeepsOtherChunks() {
        BlockPos first = new BlockPos(-16, -64, 0);
        BlockPos second = new BlockPos(-1, 96, 15);
        BlockPos other = new BlockPos(0, 32, 0);
        Map<BlockPos, BlockPos> entries = new HashMap<>(Map.of(first, BlockPos.ZERO, second, BlockPos.ZERO, other, BlockPos.ZERO));
        Set<BlockPos> invalidated = new HashSet<>();
        CTMOffsetClientHandler.applySnapshot(entries, new ChunkPos(-1, 0), Map.of(), true,
                (pos, offset) -> offset, invalidated::add);
        assertEquals(Set.of(first, second), invalidated);
        assertEquals(Set.of(other), entries.keySet());
    }

    @Test void deltasResetOnlySpecifiedPositionAndInvalidateNewEntries() {
        BlockPos reset = new BlockPos(0, 0, 0);
        BlockPos retained = new BlockPos(1, 0, 0);
        BlockPos added = new BlockPos(2, 0, 0);
        BlockPos offset = new BlockPos(3, 4, 5);
        Map<BlockPos, BlockPos> entries = new HashMap<>(Map.of(reset, offset, retained, offset));
        Set<BlockPos> invalidated = new HashSet<>();
        CTMOffsetClientHandler.applySnapshot(entries, new ChunkPos(0, 0), Map.of(reset, BlockPos.ZERO, added, offset), false,
                (pos, value) -> value, invalidated::add);
        assertEquals(Map.of(retained, offset, added, offset), entries);
        assertEquals(Set.of(reset, added), invalidated);
    }
}
