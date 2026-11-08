package io.github.chiselteam.ctm.network;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class CTMBlockOffsetSyncPacketTest {
    private static final ChunkPos CHUNK = new ChunkPos(-2, 3);

    @Test void emptySnapshotStillResetsButEmptyDeltaDoesNotSend() {
        var packets = CTMBlockOffsetSyncPacket.batches(CHUNK, Map.of(), true);
        assertEquals(1, packets.size());
        assertTrue(packets.getFirst().replace());
        assertTrue(packets.getFirst().offsets().isEmpty());
        assertTrue(CTMBlockOffsetSyncPacket.batches(CHUNK, Map.of(), false).isEmpty());
    }

    @Test void batchesPreserveEveryOffsetAndOnlyFirstReplaces() {
        Map<BlockPos, BlockPos> offsets = new HashMap<>();
        for (int i = 0; i < 16385; i++) offsets.put(new BlockPos(-32 + i % 16, i / 256, 48 + i / 16 % 16), new BlockPos(2, -1, 3));
        var packets = CTMBlockOffsetSyncPacket.batches(CHUNK, offsets, true);
        assertEquals(3, packets.size());
        assertEquals(8192, packets.get(0).offsets().size());
        assertEquals(8192, packets.get(1).offsets().size());
        assertEquals(1, packets.get(2).offsets().size());
        assertTrue(packets.get(0).replace());
        assertFalse(packets.get(1).replace());
        assertFalse(packets.get(2).replace());
        Map<BlockPos, BlockPos> restored = new HashMap<>();
        packets.forEach(packet -> restored.putAll(packet.offsets()));
        assertEquals(offsets, restored);
        offsets.clear();
        assertEquals(8192, packets.getFirst().offsets().size());
    }

    @Test void wireRoundTripRetainsNegativeCoordinatesAndZeroReset() {
        var packet = new CTMBlockOffsetSyncPacket(CHUNK, Map.of(
                new BlockPos(-31, -64, 50), new BlockPos(-5, 2, 1),
                new BlockPos(-32, 320, 49), BlockPos.ZERO), true);
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            CTMBlockOffsetSyncPacket.STREAM_CODEC.encode(buffer, packet);
            assertEquals(packet, CTMBlockOffsetSyncPacket.STREAM_CODEC.decode(buffer));
            assertFalse(buffer.isReadable());
        } finally { buffer.release(); }
    }

    @Test void invalidWireCountsAreRejected() {
        for (int count : new int[]{-1, 1048577}) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                buffer.writeInt(0);
                buffer.writeInt(0);
                buffer.writeBoolean(false);
                buffer.writeVarInt(count);
                assertThrows(IllegalArgumentException.class, () -> CTMBlockOffsetSyncPacket.STREAM_CODEC.decode(buffer));
            } finally { buffer.release(); }
        }
    }
}
