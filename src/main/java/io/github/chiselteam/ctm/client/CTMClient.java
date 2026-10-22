package io.github.chiselteam.ctm.client;

import io.github.chiselteam.ctm.CTM;
import io.github.chiselteam.ctm.client.model.CTMGeometryLoader;
import io.github.chiselteam.ctm.client.unbaked.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = CTM.MOD_ID, value = Dist.CLIENT)
public final class CTMClient {
    private CTMClient() { }

    @SubscribeEvent
    public static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(CTM.CTM_MODEL_ID, new CTMGeometryLoader(UnbakedConnectedTextureBlockStateModel.CODEC.codec()));
        event.register(CTM.LAYERED_CTM_MODEL_ID, new CTMGeometryLoader(UnbakedLayeredCTMBlockStateModel.CODEC.codec()));
        event.register(CTM.ELDRITCH_MODEL_ID, new CTMGeometryLoader(UnbakedEldritchBlockStateModel.CODEC.codec()));
    }
}
