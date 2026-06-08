package io.github.chiselteam.ctm;

import io.github.chiselteam.ctm.client.unbaked.UnbakedConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.client.unbaked.UnbakedEldritchBlockStateModel;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import io.github.chiselteam.ctm.network.CTMOffsetSyncPacket;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(CTM.MOD_ID)
public final class CTM {
    public static final String MOD_ID = "ctm";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final Identifier CTM_MODEL_ID = Identifier.fromNamespaceAndPath(CTM.MOD_ID, "connected_texture_model");
    public static final Identifier ELDRITCH_MODEL_ID = Identifier.fromNamespaceAndPath(CTM.MOD_ID, "eldritch_model");

    public CTM(IEventBus bus) {
        LOGGER.info("Loaded CTM library");
         bus.addListener(this::registerModelLoaders);
         bus.addListener(this::registerPayloads);
    }

    @SubscribeEvent
    public void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(CTM.MOD_ID).versioned("1");
        registrar.playToClient(CTMOffsetSyncPacket.TYPE, CTMOffsetSyncPacket.STREAM_CODEC, CTMOffsetSyncPacket::handle);
    }

    @SubscribeEvent
    public void registerModelLoaders(RegisterBlockStateModels event) {
        event.registerModel(CTM_MODEL_ID, UnbakedConnectedTextureBlockStateModel.CODEC);
        event.registerModel(ELDRITCH_MODEL_ID, UnbakedEldritchBlockStateModel.CODEC);
    }
}
