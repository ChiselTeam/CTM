package io.github.chiselteam.ctm.client.unbaked;

import com.mojang.datafixers.util.Pair;
import io.github.chiselteam.ctm.client.model.CTMFace.Quadrant;
import com.mojang.serialization.MapCodec;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.api.strategy.CTMLogic;
import io.github.chiselteam.ctm.api.strategy.CTMLogicAR;
import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;
import io.github.chiselteam.ctm.client.AbstractUnbakedConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.client.baked.ARCTMBlockStateModel;
import io.github.chiselteam.ctm.impl.texture.CTMTextureSet;
import io.github.chiselteam.ctm.client.model.CTMBakedModel;
import io.github.chiselteam.ctm.client.model.CTMModelVariant;
import io.github.chiselteam.ctm.client.model.CTMBakingContext;
import io.github.chiselteam.ctm.client.model.CTMFace;
import io.github.chiselteam.ctm.client.model.CTMFaceBakery;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import io.github.chiselteam.ctm.client.model.CTMUnbakedModel;
import org.joml.Vector3f;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ARUnbakedModel extends AbstractUnbakedConnectedTextureBlockStateModel {

    public ARUnbakedModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, ResourceLocation> textureSlots, CTMModelVariant.SimpleModelState modelState) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots, modelState);
    }

    public ARUnbakedModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, ResourceLocation> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots);
    }

    public ARUnbakedModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, ResourceLocation> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, eldritch, connectionPredicate, overlays, textureSlots);
    }

    public ARUnbakedModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity);
    }

    @Override
    public @NotNull MapCodec<? extends CTMUnbakedModel> codec() {
        return UnbakedConnectedTextureBlockStateModel.CODEC;
    }

    @Override
    public @NotNull CTMBakedModel bake(@NotNull CTMBakingContext baker) {
        baker = baker.withAmbientOcclusion(ambientOcclusion);
        prepareBakery(baker);

        Material baseMaterial = getMaterial("base_texture");
        if (baseMaterial == null) baseMaterial = getMaterial("layer0");
        var particleMaterial = getMaterial("particle");

        var bakedBase = bakeMaterial(baker, baseMaterial);
        var bakedParticle = bakeMaterial(baker, particleMaterial);

        var textures = bakeTextures(baker);
        textures.requireComplete(modelLocation.toString());
        if (bakedParticle == null) bakedParticle = bakedBase != null ? bakedBase : textures.get(CTMLogicAR.T0);

        Map<Direction, BakedQuad[]> baseQuads = new EnumMap<>(Direction.class);
        Map<Direction, BakedQuad[][]> connectedQuads = new EnumMap<>(Direction.class);
        Set<Direction> unculledFaces = new HashSet<>();

        var from = element.getFirst();
        var to = element.getSecond();
        int center = 8;

        for (var face : Direction.values()) {
            var cull = getCullface(face, from, to);
            var planeDirections = CTMLogic.AXIS_PLANE_DIRECTIONS[face.getAxis().ordinal()];

            var baseQuadList = new ArrayList<BakedQuad>();
            var connQuads = new BakedQuad[4][CTMLogicAR.values().length];

            for (int cornerIndex = 0; cornerIndex < 4; cornerIndex++) {
                var corner = face.getNormal()
                        .offset(planeDirections[cornerIndex].getNormal())
                        .offset(planeDirections[(cornerIndex + 1) % 4].getNormal())
                        .offset(1, 1, 1)
                        .multiply(8);

                var qFrom = new Vector3f(
                        Math.clamp(Math.min(center - (16 - to.x()), (float) corner.getX() + from.x()), 0, 16),
                        Math.clamp(Math.min(center - (16 - to.y()), (float) corner.getY() + from.y()), 0, 16),
                        Math.clamp(Math.min(center - (16 - to.z()), (float) corner.getZ() + from.z()), 0, 16)
                );

                var qTo = new Vector3f(
                        to.x() < center ? to.x() : Math.max(center, (float) corner.getX() - (16 - to.x())),
                        to.y() < center ? to.y() : Math.max(center, (float) corner.getY() - (16 - to.y())),
                        to.z() < center ? to.z() : Math.max(center, (float) corner.getZ() - (16 - to.z()))
                );

                var qUvs = getRelativeUVs(face, qFrom, qTo);
                var offsets = getOffsets(face, qFrom, qTo);

                if (bakedBase != null) {
                    var baseFace = new CTMFace(cull, baseTintIndex, "", qUvs, Quadrant.R0);
                    baseQuadList.add(CTMFaceBakery.bakeQuad(baker, qFrom, qTo, baseFace, bakedBase, face, state, null, shade, baseEmissivity));
                }

                bakeQuads(baker, face, cull, qUvs, offsets, textures, connQuads[cornerIndex], unculledFaces);
            }

            if (!baseQuadList.isEmpty()) baseQuads.put(face, baseQuadList.toArray(new BakedQuad[0]));
            connectedQuads.put(face, connQuads);
        }

        var bakedOverlays = bakeOverlays();
        return new ARCTMBlockStateModel(
                remapDirections(connectedFaces, connectedQuads),
                remapDirections(unculledFaces, connectedQuads),
                renderOverlayOnAllFaces,
                remapQuadDirections(baseQuads),
                remapQuadDirections(connectedQuads),
                bakedParticle != null ? bakedParticle.sprite() : null,
                variant,
                connectionPredicate,
                bakedOverlays,
                remapRuleQuadDirections(bakeOverlayQuads(baker, bakedOverlays, from, to, state)),
                ambientOcclusion
        );
    }

    private CTMTextureSet<CTMLogicAR> bakeTextures(CTMBakingContext baker) {
        var textures = new CTMTextureSet<>(CTMLogicAR.class);
        textures.put(CTMLogicAR.T0, bakeMaterial(baker, getMaterial(CTMTextureKeys.AR_1)));
        textures.put(CTMLogicAR.T1, bakeMaterial(baker, getMaterial(CTMTextureKeys.AR_2)));
        textures.put(CTMLogicAR.T2, bakeMaterial(baker, getMaterial(CTMTextureKeys.AR_3)));
        textures.put(CTMLogicAR.T3, bakeMaterial(baker, getMaterial(CTMTextureKeys.AR_4)));
        return textures;
    }

    private void bakeQuads(CTMBakingContext baker, Direction face, Direction cull, CTMFace.UVs faceUVs, Vector3f[] offsets, CTMTextureSet<CTMLogicAR> textures, BakedQuad[] connQuads, Set<Direction> unculledFaces) {
        for (var logic : CTMLogicAR.values()) {
            var mat = textures.get(logic);
            if (mat == null) continue;

            var connFace = new CTMFace(cull, tintIndex, "", faceUVs, Quadrant.R0);
            if (connFace.cullForDirection() == null) unculledFaces.add(face);

            connQuads[logic.ordinal()] = CTMFaceBakery.bakeQuad(baker, offsets[0], offsets[1], connFace, mat, face, state, null, shade, emissivity);
        }
    }
}
