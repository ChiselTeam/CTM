package io.github.chiselteam.ctm;

import io.github.chiselteam.ctm.api.datagen.CTMBlockModelDefinition;
import io.github.chiselteam.ctm.api.datagen.LayeredCTMBlockModelDefinition;
import io.github.chiselteam.ctm.api.texture.CTMBlockOffsets;
import io.github.chiselteam.ctm.client.unbaked.UnbakedConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.client.unbaked.UnbakedLayeredCTMBlockStateModel;
import io.github.chiselteam.ctm.client.unbaked.UnbakedEldritchBlockStateModel;
import io.github.chiselteam.ctm.network.CTMBlockOffsetSyncPacket;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import io.github.chiselteam.ctm.network.CTMOffsetSyncPacket;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(CTM.MOD_ID)
public final class CTM {
    public static final String MOD_ID = "ctm";

    public static final Identifier CTM_MODEL_ID = Identifier.fromNamespaceAndPath(CTM.MOD_ID, "connected_texture_model");
    public static final Identifier ELDRITCH_MODEL_ID = Identifier.fromNamespaceAndPath(CTM.MOD_ID, "eldritch_model");
    public static final Identifier CTM_DEFINITION_ID = Identifier.fromNamespaceAndPath(CTM.MOD_ID, "connected_texture_model");
    public static final Identifier LAYERED_CTM_MODEL_ID = Identifier.fromNamespaceAndPath(CTM.MOD_ID, "layered_connected_texture_model");

    public CTM(IEventBus bus) {
         bus.addListener(this::registerModelLoaders);
         bus.addListener(this::registerPayloads);
         NeoForge.EVENT_BUS.addListener(
             (ChunkWatchEvent.Sent event) -> {
                 var offsets = CTMBlockOffsets.getOffsets(event.getLevel(), event.getPos());
                 CTMBlockOffsetSyncPacket.batches(event.getPos(), offsets, true).forEach(
                     packet -> PacketDistributor.sendToPlayer(event.getPlayer(), packet));
             });
    }

    @SubscribeEvent
    public void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(CTM.MOD_ID).versioned("1");
        registrar.playToClient(CTMBlockOffsetSyncPacket.TYPE, CTMBlockOffsetSyncPacket.STREAM_CODEC, CTMBlockOffsetSyncPacket::handle);
        registrar.playToClient(CTMOffsetSyncPacket.TYPE, CTMOffsetSyncPacket.STREAM_CODEC, CTMOffsetSyncPacket::handle);
    }

    @SubscribeEvent
    public void registerModelLoaders(RegisterBlockStateModels event) {
        event.registerModel(CTM_MODEL_ID, UnbakedConnectedTextureBlockStateModel.CODEC);
        event.registerModel(LAYERED_CTM_MODEL_ID, UnbakedLayeredCTMBlockStateModel.CODEC);
        event.registerModel(ELDRITCH_MODEL_ID, UnbakedEldritchBlockStateModel.CODEC);
        event.registerDefinition(CTM_DEFINITION_ID, CTMBlockModelDefinition.CODEC);
        event.registerDefinition(LAYERED_CTM_MODEL_ID, LayeredCTMBlockModelDefinition.CODEC);
    }
}
