package io.github.chiselteam.ctm.impl.model;

import com.mojang.logging.LogUtils;
import io.github.chiselteam.ctm.CTM;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.slf4j.Logger;

import java.util.*;

@EventBusSubscriber(modid = CTM.MOD_ID, value = Dist.CLIENT)
public final class CTMQuadDeduplicator {
    private static final Logger LOGGER = LogUtils.getLogger();

    private CTMQuadDeduplicator() {}

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        event.getBakingResult().blockStateModels().forEach((state, model) -> {
            if (model == null) {
                var blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                LOGGER.warn("Skipping null baked model for block state {} (block: {}, namespace: {}). "
                                + "The namespace identifies the block owner; another mod may have replaced its model.",
                        state, blockId, blockId.getNamespace());
            }
        });
        deduplicate(event.getBakingResult().blockStateModels().values());
    }

    public static void deduplicate(Collection<? extends BlockStateModel> models) {
        Map<BakedQuad, BakedQuad> canonical = new HashMap<>();
        Set<BlockStateModel> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        var pending = new ArrayDeque<BlockStateModel>();
        for (var model : models) {
            if (model != null) {
                pending.add(model);
            }
        }
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
            storage.visitChildModels(child -> {
                if (child == null) {
                    var modelClass = model.getClass();
                    var source = modelClass.getProtectionDomain().getCodeSource();
                    LOGGER.warn("Skipping null child model supplied by parent model {} (source: {}).",
                            modelClass.getName(), source == null ? "unknown" : source.getLocation());
                } else {
                    pending.addLast(child);
                }
            });
        }
    }
}
