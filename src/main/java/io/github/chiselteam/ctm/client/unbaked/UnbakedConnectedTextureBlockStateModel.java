package io.github.chiselteam.ctm.client.unbaked;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.api.strategy.CTMKind;
import io.github.chiselteam.ctm.client.AbstractUnbakedConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.client.baked.EldritchBlockStateModel;
import io.github.chiselteam.ctm.client.model.CTMBakedModel;
import io.github.chiselteam.ctm.client.model.CTMModelVariant;
import io.github.chiselteam.ctm.api.datagen.VariantMutator;
import io.github.chiselteam.ctm.client.model.CTMBakingContext;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import io.github.chiselteam.ctm.client.model.CTMUnbakedModel;
import org.joml.Vector3f;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class UnbakedConnectedTextureBlockStateModel extends AbstractUnbakedConnectedTextureBlockStateModel {

    public UnbakedConnectedTextureBlockStateModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, ResourceLocation> textureSlots, CTMModelVariant.SimpleModelState modelState) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots, modelState);
    }

    public UnbakedConnectedTextureBlockStateModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, ResourceLocation> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots);
    }

    public UnbakedConnectedTextureBlockStateModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, ResourceLocation> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, eldritch, connectionPredicate, overlays, textureSlots);
    }

    public UnbakedConnectedTextureBlockStateModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, ResourceLocation> textureSlots) {
        this(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, true, eldritch, connectionPredicate, overlays, textureSlots);
    }

    public UnbakedConnectedTextureBlockStateModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity) {
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
                    ResourceLocation.CODEC.fieldOf("model_location").forGetter(m -> m.modelLocation),
                    ELEMENT_CODEC.optionalFieldOf("element", DEFAULT_ELEMENT).forGetter(m -> m.element),
                    CTMModelCodecs.FACES_CODEC.fieldOf("connected_faces").forGetter(m -> m.connectedFaces),
                    Codec.BOOL.optionalFieldOf("render_overlay_on_all_faces", false).forGetter(m -> m.renderOverlayOnAllFaces),
                    CTMVariant.CODEC.fieldOf("variant").forGetter(m -> m.variant),
                    Codec.INT.optionalFieldOf("base_tint_index", -1).forGetter(m -> m.baseTintIndex),
                    Codec.INT.optionalFieldOf("base_emissivity", 0).forGetter(m -> m.baseEmissivity),
                    Codec.INT.optionalFieldOf("tint_index", -1).forGetter(m -> m.tintIndex),
                    Codec.INT.optionalFieldOf("emissivity", 0).forGetter(m -> m.emissivity),
                    Codec.BOOL.optionalFieldOf("shade", true).forGetter(m -> m.shade),
                    Codec.BOOL.optionalFieldOf("ambient_occlusion", true).forGetter(m -> m.ambientOcclusion),
                    Codec.BOOL.optionalFieldOf("eldritch", false).forGetter(m -> m.eldritch),
                    CTMModelCodecs.CONNECTS_TO_CODEC.optionalFieldOf("connects_to", CTMBlockPredicate.sameBlock()).forGetter(m -> m.connectionPredicate),
                    CTMModelCodecs.OVERLAY_RULE_CODEC.listOf().optionalFieldOf("overlays", List.of()).forGetter(m -> m.overlays),
                    Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC).optionalFieldOf("texture_slots", Map.of()).forGetter(m -> m.textureSlots),
                    CTMModelVariant.SimpleModelState.MAP_CODEC.forGetter(m -> m.modelState)
            ).apply(instance, UnbakedConnectedTextureBlockStateModel::new)
    );

    public UnbakedConnectedTextureBlockStateModel with(VariantMutator mutator) {
        CTMModelVariant transformed = mutator.apply(new CTMModelVariant(modelLocation, modelState));
        return new UnbakedConnectedTextureBlockStateModel(transformed.modelLocation(), element, connectedFaces,
                renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade,
                ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots, transformed.modelState());
    }

    public CTMModelVariant.SimpleModelState modelState() {
        return modelState;
    }

    public ResourceLocation modelLocation() {
        return modelLocation;
    }

    @Override
    public @NotNull MapCodec<? extends CTMUnbakedModel> codec() {
        return CODEC;
    }

    @Override
    public @NotNull CTMBakedModel bake(@NotNull CTMBakingContext baker) {
        CTMBakedModel baked = forKind(variant.kind()).bake(baker);
        return eldritch ? new EldritchBlockStateModel(baked) : baked;
    }

    private AbstractUnbakedConnectedTextureBlockStateModel forKind(CTMKind kind) {
        return switch (kind) {
            case STANDARD -> new StandardUnbakedCTMModel(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots, modelState);
            case TBS -> new TBSUnbakedCTMModel(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots, modelState);
            case AR -> new ARUnbakedModel(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots, modelState);
            case BOOKSHELF, CTMH, CTMV -> new DirectionalUnbakedCTMModel(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots, modelState);
            case EDGES, EDGES_FULL -> new EdgesUnbakedCTMModel(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots, modelState);
            case MULTIBLOCK_2X2, MULTIBLOCK_3X3, MULTIBLOCK_4X4,
                 V4, V9, V16,
                 R4, R9, R16 -> new MultiblockUnbakedCTMModel(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots, modelState);
        };
    }
}
