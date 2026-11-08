package io.github.chiselteam.ctm.network;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CTMBlockOffsetSyncPacketTest {
    @Test void batchesPreserveAllEntriesAndReplaceOnlyOnFirstPacket() {
        Map<BlockPos, BlockPos> offsets = new HashMap<>();
        for (int i = 0; i < 16385; i++) offsets.put(new BlockPos(0, i, 0), new BlockPos(1, 2, 3));
        var packets = CTMBlockOffsetSyncPacket.batches(new ChunkPos(0, 0), offsets, true);
        assertEquals(List.of(8192, 8192, 1), packets.stream().map(p -> p.offsets().size()).toList());
        assertEquals(List.of(true, false, false), packets.stream().map(CTMBlockOffsetSyncPacket::replace).toList());
        Map<BlockPos, BlockPos> restored = new HashMap<>();
        packets.forEach(p -> restored.putAll(p.offsets()));
        assertEquals(offsets, restored);
        offsets.clear();
        assertEquals(8192, packets.getFirst().offsets().size());
    }
    @Test void emptyReplacementClearsClientWhileEmptyDeltaSendsNothing() {
        assertEquals(1, CTMBlockOffsetSyncPacket.batches(new ChunkPos(0, 0), Map.of(), true).size());
        assertTrue(CTMBlockOffsetSyncPacket.batches(new ChunkPos(0, 0), Map.of(), false).isEmpty());
    }
    @Test void codecRoundTripsNegativeCoordinatesAndReset() {
        var packet = new CTMBlockOffsetSyncPacket(new ChunkPos(-2, 3), Map.of(new BlockPos(-17, -64, 48), BlockPos.ZERO), true);
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            CTMBlockOffsetSyncPacket.STREAM_CODEC.encode(buffer, packet);
            assertEquals(packet, CTMBlockOffsetSyncPacket.STREAM_CODEC.decode(buffer));
            assertEquals(0, buffer.readableBytes());
        } finally { buffer.release(); }
    }
    @Test void codecRejectsInvalidCount() {
        for (int count : new int[]{-1, 1048577}) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                buffer.writeInt(0); buffer.writeInt(0); buffer.writeBoolean(false); buffer.writeVarInt(count);
                assertThrows(IllegalArgumentException.class, () -> CTMBlockOffsetSyncPacket.STREAM_CODEC.decode(buffer));
            } finally { buffer.release(); }
        }
    }
}
