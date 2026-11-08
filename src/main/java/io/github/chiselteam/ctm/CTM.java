package io.github.chiselteam.ctm;

import io.github.chiselteam.ctm.network.CTMOffsetSyncPacket;
import io.github.chiselteam.ctm.network.CTMBlockOffsetSyncPacket;
import io.github.chiselteam.ctm.api.texture.CTMBlockOffsets;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(CTM.MOD_ID)
public final class CTM {
    public static final String MOD_ID = "ctm";
    public static final ResourceLocation CTM_MODEL_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "connected_texture_model");
    public static final ResourceLocation ELDRITCH_MODEL_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "eldritch_model");
    public static final ResourceLocation LAYERED_CTM_MODEL_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "layered_connected_texture_model");

    public CTM(IEventBus bus) {
        bus.addListener(CTM::registerPayloads);
        NeoForge.EVENT_BUS.addListener((ChunkWatchEvent.Sent event) -> {
            var offsets = CTMBlockOffsets.getOffsets(event.getLevel(), event.getPos());
            CTMBlockOffsetSyncPacket.batches(event.getPos(), offsets, true).forEach(
                    packet -> PacketDistributor.sendToPlayer(event.getPlayer(), packet));
        });
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(CTMBlockOffsetSyncPacket.TYPE, CTMBlockOffsetSyncPacket.STREAM_CODEC, CTMBlockOffsetSyncPacket::handle);
        event.registrar("1").playToClient(CTMOffsetSyncPacket.TYPE, CTMOffsetSyncPacket.STREAM_CODEC, CTMOffsetSyncPacket::handle);
    }
}
