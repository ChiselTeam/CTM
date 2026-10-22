package io.github.chiselteam.ctm.impl.model;

import io.github.chiselteam.ctm.CTM;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = CTM.MOD_ID, value = Dist.CLIENT)
public final class CTMQuadDeduplicator {
    private CTMQuadDeduplicator() {}

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        deduplicate(event.getBakingResult().blockStateModels().values());
    }

    public static void deduplicate(Collection<? extends BlockStateModel> models) {
        Map<BakedQuad, BakedQuad> canonical = new HashMap<>();
        Set<BlockStateModel> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        var pending = new ArrayDeque<BlockStateModel>(models);
        while (!pending.isEmpty()) {
            var model = pending.removeFirst();
            if (!visited.add(model) || !(model instanceof CTMQuadStorage storage)) continue;
            storage.visitQuadArrays(quads -> {
                if (quads == null) return;
                for (int i = 0; i < quads.length; i++) {
                    var quad = quads[i];
                    if (quad == null) continue;
                    var existing = canonical.putIfAbsent(quad, quad);
                    if (existing != null) quads[i] = existing;
                }
            });
            storage.visitChildModels(pending::addLast);
        }
    }
}
