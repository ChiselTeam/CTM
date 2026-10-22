package io.github.chiselteam.ctm;

import io.github.chiselteam.ctm.network.CTMOffsetSyncPacket;
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
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(CTMOffsetSyncPacket.TYPE, CTMOffsetSyncPacket.STREAM_CODEC, CTMOffsetSyncPacket::handle);
    }
}
