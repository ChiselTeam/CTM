package io.github.chiselteam.ctm.network;

import io.github.chiselteam.ctm.CTM;
import io.github.chiselteam.ctm.client.CTMOffsetClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

public record CTMOffsetSyncPacket(ChunkPos chunkPos, BlockPos offset) implements CustomPacketPayload {

    public static final Type<CTMOffsetSyncPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(CTM.MOD_ID, "offset_sync"));

    public static final StreamCodec<FriendlyByteBuf, CTMOffsetSyncPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NonNull CTMOffsetSyncPacket decode(@NonNull FriendlyByteBuf buf) {
            return new CTMOffsetSyncPacket(new ChunkPos(buf.readInt(), buf.readInt()), buf.readBlockPos());
        }

        @Override
        public void encode(@NonNull FriendlyByteBuf buf, @NonNull CTMOffsetSyncPacket packet) {
            buf.writeInt(packet.chunkPos.x());
            buf.writeInt(packet.chunkPos.z());
            buf.writeBlockPos(packet.offset);
        }
    };

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final CTMOffsetSyncPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            CTMOffsetClientHandler.setOffset(payload.chunkPos(), payload.offset());
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc.level;
            if (level != null) {
                int cx = payload.chunkPos().x();
                int cz = payload.chunkPos().z();
                int minSection = level.getMinSectionY();
                int maxSection = level.getMaxSectionY();
                for (int sy = minSection; sy <= maxSection; sy++) {
                    mc.levelRenderer.setSectionDirty(cx, sy, cz);
                }
            }
        });
    }
}
