package io.github.chiselteam.ctm.client.unbaked;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quadrant;
import com.mojang.serialization.MapCodec;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.api.strategy.CTMLogic;
import io.github.chiselteam.ctm.api.strategy.CTMLogicAR;
import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;
import io.github.chiselteam.ctm.client.AbstractUnbakedConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.client.baked.ARCTMBlockStateModel;
import io.github.chiselteam.ctm.impl.texture.CTMTextureSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class ARUnbakedModel extends AbstractUnbakedConnectedTextureBlockStateModel {

    public ARUnbakedModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots, Variant.SimpleModelState modelState) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots, modelState);
    }

    public ARUnbakedModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots);
    }

    public ARUnbakedModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, eldritch, connectionPredicate, overlays, textureSlots);
    }

    public ARUnbakedModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity);
    }

    @Override
    public @NonNull MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return UnbakedConnectedTextureBlockStateModel.CODEC;
    }

    @Override
    public @NonNull BlockStateModel bake(@NonNull ModelBaker baker) {
        prepareBakery(baker);

        var textureSlots = model.getTopTextureSlots();
        Material baseMaterial = textureSlots.getMaterial("base_texture");
        if (baseMaterial == null) baseMaterial = textureSlots.getMaterial("layer0");
        var particleMaterial = textureSlots.getMaterial("particle");

        var bakedBase = bakeMaterial(baker, baseMaterial);
        var bakedParticle = bakeMaterial(baker, particleMaterial);

        var textures = bakeTextures(baker);
        var useStandaloneTextures = textures.isComplete();

        Material.Baked legacyOverlay = null;
        if (!useStandaloneTextures) legacyOverlay = bakeLegacyOverlay(baker);

        if (bakedParticle == null) {
            if (bakedBase != null) bakedParticle = bakedBase;
            else if (useStandaloneTextures) bakedParticle = textures.get(CTMLogicAR.T0);
            else bakedParticle = legacyOverlay;
        }

        Map<Direction, BakedQuad[]> baseQuads = new EnumMap<>(Direction.class);
        Map<Direction, BakedQuad[][]> connectedQuads = new EnumMap<>(Direction.class);
        Set<Direction> unculledFaces = new HashSet<>();

        var from = element.getFirst();
        var to = element.getSecond();
        int center = 8;

        for (var face : Direction.values()) {
            var cull = getCullface(face, from, to);
            var planeDirections = CTMLogic.AXIS_PLANE_DIRECTIONS[face.getAxis().ordinal()];

            var baseQuadList = new ArrayList<>();
            var connQuads = new BakedQuad[4][CTMLogicAR.values().length];

            for (int cornerIndex = 0; cornerIndex < 4; cornerIndex++) {
                var corner = face.getUnitVec3i()
                        .offset(planeDirections[cornerIndex].getUnitVec3i())
                        .offset(planeDirections[(cornerIndex + 1) % 4].getUnitVec3i())
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
                    var baseFace = new CuboidFace(cull, baseTintIndex, "", qUvs, Quadrant.R0);
                    baseQuadList.add(FaceBakery.bakeQuad(baker, qFrom, qTo, baseFace, bakedBase, face, state, null, shade, baseEmissivity));
                }

                if (useStandaloneTextures) {
                    bakeQuads(baker, face, cull, qUvs, offsets, textures, connQuads[cornerIndex], unculledFaces);
                } else if (legacyOverlay != null) {
                    bakeLegacyARQuads(baker, face, cull, qUvs, offsets, legacyOverlay, connQuads[cornerIndex], unculledFaces);
                }
            }
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

    private CTMTextureSet<CTMLogicAR> bakeTextures(ModelBaker baker) {
        var textures = new CTMTextureSet<>(CTMLogicAR.class);
        textures.put(CTMLogicAR.T0, bakeMaterial(baker, getMaterial(CTMTextureKeys.AR_1)));
        textures.put(CTMLogicAR.T1, bakeMaterial(baker, getMaterial(CTMTextureKeys.AR_2)));
        textures.put(CTMLogicAR.T2, bakeMaterial(baker, getMaterial(CTMTextureKeys.AR_3)));
        textures.put(CTMLogicAR.T3, bakeMaterial(baker, getMaterial(CTMTextureKeys.AR_4)));
        return textures;
    }

    @Deprecated(forRemoval = true, since = "26.1")
    private Material.Baked bakeLegacyOverlay(ModelBaker baker) {
        Material overlay = getMaterial("overlay_2x2");
        if (overlay == null) overlay = getMaterial("overlay_texture");
        if (overlay == null) overlay = getMaterial("layer1");

        return bakeMaterial(baker, overlay);
    }

    private void bakeQuads(ModelBaker baker, Direction face, Direction cull, CuboidFace.UVs faceUVs, Vector3f[] offsets, CTMTextureSet<CTMLogicAR> textures, BakedQuad[] connQuads, Set<Direction> unculledFaces) {
        for (var logic : CTMLogicAR.values()) {
            var mat = textures.get(logic);
            if (mat == null) continue;

            var connFace = new CuboidFace(cull, tintIndex, "", faceUVs, Quadrant.R0);
            if (connFace.cullForDirection() == null) unculledFaces.add(face);

            connQuads[logic.ordinal()] = FaceBakery.bakeQuad(baker, offsets[0], offsets[1], connFace, mat, face, state, null, shade, emissivity);
        }
    }

    @Deprecated(forRemoval = true, since = "26.1")
    private void bakeLegacyARQuads(ModelBaker baker, Direction face, Direction cull, CuboidFace.UVs qUvs, Vector3f[] offsets, Material.Baked material, BakedQuad[] connQuads, Set<Direction> unculledFaces) {
        int[][] arUVs = {
                {0, 0},
                {16, 0},
                {0, 16},
                {16, 16}
        };

        for (int i = 0; i < arUVs.length; i++) {
            int u = arUVs[i][0];
            int v = arUVs[i][1];

            var arUvs = new CuboidFace.UVs(
                    (u + qUvs.minU()) / 2.0F,
                    (v + qUvs.minV()) / 2.0F,
                    (u + qUvs.maxU()) / 2.0F,
                    (v + qUvs.maxV()) / 2.0F
            );

            var connFace = new CuboidFace(cull, tintIndex, "", arUvs, Quadrant.R0);
            if (connFace.cullForDirection() == null) unculledFaces.add(face);

            connQuads[i] = FaceBakery.bakeQuad(baker, offsets[0], offsets[1], connFace, material, face, state, null, shade, emissivity);
        }
    }
}
