package io.github.chiselteam.ctm.network;

import io.github.chiselteam.ctm.CTM;
import io.github.chiselteam.ctm.client.CTMOffsetClientHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public record CTMBlockOffsetSyncPacket(ChunkPos chunk, Map<BlockPos, BlockPos> offsets, boolean replace) implements CustomPacketPayload {
    public CTMBlockOffsetSyncPacket(ChunkPos chunk, Map<BlockPos, BlockPos> offsets) { this(chunk, offsets, false); }
    public CTMBlockOffsetSyncPacket { offsets = Map.copyOf(offsets); }
    public static final Type<CTMBlockOffsetSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CTM.MOD_ID, "block_offset_sync"));
    public static final StreamCodec<FriendlyByteBuf, CTMBlockOffsetSyncPacket> STREAM_CODEC = new StreamCodec<>() {
        public CTMBlockOffsetSyncPacket decode(FriendlyByteBuf buf) {
            ChunkPos chunk = new ChunkPos(buf.readInt(), buf.readInt());
            boolean replace = buf.readBoolean();
            int count = buf.readVarInt();
            if (count < 0 || count > 1048576) throw new IllegalArgumentException("Invalid offset count");
            Map<BlockPos, BlockPos> offsets = new HashMap<>();
            for (int i = 0; i < count; i++) offsets.put(buf.readBlockPos(), buf.readBlockPos());
            return new CTMBlockOffsetSyncPacket(chunk, offsets, replace);
        }
        public void encode(FriendlyByteBuf buf, CTMBlockOffsetSyncPacket packet) {
            buf.writeInt(packet.chunk.x); buf.writeInt(packet.chunk.z);
            buf.writeBoolean(packet.replace);
            buf.writeVarInt(packet.offsets.size());
            packet.offsets.forEach((pos, offset) -> { buf.writeBlockPos(pos); buf.writeBlockPos(offset); });
        }
    };
    /** Keep even large chunk snapshots well below the client payload size limit. */
    public static List<CTMBlockOffsetSyncPacket> batches(ChunkPos chunk, Map<BlockPos, BlockPos> offsets, boolean replace) {
        List<CTMBlockOffsetSyncPacket> packets = new ArrayList<>();
        Map<BlockPos, BlockPos> batch = new HashMap<>();
        for (var entry : offsets.entrySet()) {
            if (batch.size() == 8192) {
                packets.add(new CTMBlockOffsetSyncPacket(chunk, batch, replace));
                replace = false;
                batch.clear();
            }
            batch.put(entry.getKey(), entry.getValue());
        }
        if (!batch.isEmpty() || replace) packets.add(new CTMBlockOffsetSyncPacket(chunk, batch, replace));
        return List.copyOf(packets);
    }

    public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(CTMBlockOffsetSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> CTMOffsetClientHandler.setBlockOffsets(packet.chunk(), packet.offsets(), packet.replace()));
    }
}
