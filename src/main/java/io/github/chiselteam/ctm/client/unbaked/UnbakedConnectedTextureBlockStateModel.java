package io.github.chiselteam.ctm.client.unbaked;

import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.api.strategy.CTMKind;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.chiselteam.ctm.client.AbstractUnbakedConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.client.baked.EldritchBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class UnbakedConnectedTextureBlockStateModel extends AbstractUnbakedConnectedTextureBlockStateModel {

    public UnbakedConnectedTextureBlockStateModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, eldritch, connectionPredicate, overlays, textureSlots);
    }

    public UnbakedConnectedTextureBlockStateModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity) {
        this(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, false, CTMBlockPredicate.sameBlock(), List.of(), Map.of());
    }

    private static final Codec<Vector3f> VECTOR3F_CODEC = Codec.FLOAT.listOf().comapFlatMap(
            list -> list.size() == 3 ? DataResult.success(new Vector3f(list.getFirst(), list.get(1), list.get(2))) : DataResult.error(() -> "Vector3f must have 3 components"),
            vec -> List.of(vec.x(), vec.y(), vec.z())
    );

    private static final Codec<Pair<Vector3f, Vector3f>> ELEMENT_CODEC = RecordCodecBuilder.create(j -> j.group(
            VECTOR3F_CODEC.fieldOf("min").forGetter(Pair::getFirst),
            VECTOR3F_CODEC.fieldOf("max").forGetter(Pair::getSecond)
    ).apply(j, Pair::of));

    private static final Pair<Vector3f, Vector3f> DEFAULT_ELEMENT = Pair.of(new Vector3f(0, 0, 0), new Vector3f(16, 16, 16));

    public static final MapCodec<UnbakedConnectedTextureBlockStateModel> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Identifier.CODEC.fieldOf("model_location").forGetter(m -> m.modelLocation),
                    ELEMENT_CODEC.optionalFieldOf("element", DEFAULT_ELEMENT).forGetter(m -> m.element),
                    CTMModelCodecs.FACES_CODEC.fieldOf("connected_faces").forGetter(m -> m.connectedFaces),
                    Codec.BOOL.optionalFieldOf("render_overlay_on_all_faces", false).forGetter(m -> m.renderOverlayOnAllFaces),
                    CTMVariant.CODEC.fieldOf("variant").forGetter(m -> m.variant),
                    Codec.INT.optionalFieldOf("base_tint_index", -1).forGetter(m -> m.baseTintIndex),
                    Codec.INT.optionalFieldOf("base_emissivity", 0).forGetter(m -> m.baseEmissivity),
                    Codec.INT.optionalFieldOf("tint_index", -1).forGetter(m -> m.tintIndex),
                    Codec.INT.optionalFieldOf("emissivity", 0).forGetter(m -> m.emissivity),
                    Codec.BOOL.optionalFieldOf("eldritch", false).forGetter(m -> m.eldritch),
                    CTMModelCodecs.CONNECTS_TO_CODEC.optionalFieldOf("connects_to", CTMBlockPredicate.sameBlock()).forGetter(m -> m.connectionPredicate),
                    CTMModelCodecs.OVERLAY_RULE_CODEC.listOf().optionalFieldOf("overlays", List.of()).forGetter(m -> m.overlays),
                    Codec.unboundedMap(Codec.STRING, Identifier.CODEC).optionalFieldOf("texture_slots", Map.of()).forGetter(m -> m.textureSlots)
            ).apply(instance, (Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, Boolean renderOverlayOnAllFaces, CTMVariant variant, Integer baseTintIndex, Integer baseEmissivity, Integer tintIndex, Integer emissivity, Boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots) ->
                    new UnbakedConnectedTextureBlockStateModel(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, eldritch, connectionPredicate, overlays, textureSlots))
    );

    @Override
    public @NonNull MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return CODEC;
    }

    @Override
    public @NonNull BlockStateModel bake(@NonNull ModelBaker baker) {
        BlockStateModel baked = forKind(variant.kind()).bake(baker);
        return eldritch ? new EldritchBlockStateModel(baked) : baked;
    }

    private AbstractUnbakedConnectedTextureBlockStateModel forKind(CTMKind kind) {
        return switch (kind) {
            case STANDARD -> new StandardUnbakedCTMModel(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, eldritch, connectionPredicate, overlays, textureSlots);
            case TBS -> new TBSUnbakedCTMModel(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, eldritch, connectionPredicate, overlays, textureSlots);
            case AR -> new ARUnbakedModel(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, eldritch, connectionPredicate, overlays, textureSlots);
            case BOOKSHELF, CTMH, CTMV -> new DirectionalUnbakedCTMModel(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, eldritch, connectionPredicate, overlays, textureSlots);
            case EDGES, EDGES_FULL -> new EdgesUnbakedCTMModel(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, eldritch, connectionPredicate, overlays, textureSlots);
            case MULTIBLOCK_2X2, MULTIBLOCK_3X3, MULTIBLOCK_4X4,
                 V4, V9, V16,
                 R4, R9, R16 -> new MultiblockUnbakedCTMModel(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, eldritch, connectionPredicate, overlays, textureSlots);
        };
    }
}
