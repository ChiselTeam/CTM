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
import org.jspecify.annotations.Nullable;

import java.util.*;

public class ARUnbakedModel extends AbstractUnbakedConnectedTextureBlockStateModel {

    public ARUnbakedModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, @Nullable Direction shadeDirectionOverride, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots, Variant.SimpleModelState modelState) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shadeDirectionOverride, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots, modelState);
    }

    public ARUnbakedModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, @Nullable Direction shadeDirectionOverride, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shadeDirectionOverride, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots);
    }

    public ARUnbakedModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, @Nullable Direction shadeDirectionOverride, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shadeDirectionOverride, eldritch, connectionPredicate, overlays, textureSlots);
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

        if (bakedParticle == null) {
            if (bakedBase != null) bakedParticle = bakedBase;
            else if (useStandaloneTextures) bakedParticle = textures.get(CTMLogicAR.T0);
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

            var baseQuadList = new ArrayList<BakedQuad>();
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
                    baseQuadList.add(FaceBakery.bakeQuad(baker, qFrom, qTo, baseFace, bakedBase, face, state, null, shadeDirectionOverride, baseEmissivity));
                }

                if (useStandaloneTextures) {
                    bakeQuads(baker, face, cull, qUvs, offsets, textures, connQuads[cornerIndex], unculledFaces);
                }
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

    private CTMTextureSet<CTMLogicAR> bakeTextures(ModelBaker baker) {
        var textures = new CTMTextureSet<>(CTMLogicAR.class);
        textures.put(CTMLogicAR.T0, bakeMaterial(baker, getMaterial(CTMTextureKeys.AR_1)));
        textures.put(CTMLogicAR.T1, bakeMaterial(baker, getMaterial(CTMTextureKeys.AR_2)));
        textures.put(CTMLogicAR.T2, bakeMaterial(baker, getMaterial(CTMTextureKeys.AR_3)));
        textures.put(CTMLogicAR.T3, bakeMaterial(baker, getMaterial(CTMTextureKeys.AR_4)));
        return textures;
    }

    private void bakeQuads(ModelBaker baker, Direction face, Direction cull, CuboidFace.UVs faceUVs, Vector3f[] offsets, CTMTextureSet<CTMLogicAR> textures, BakedQuad[] connQuads, Set<Direction> unculledFaces) {
        for (var logic : CTMLogicAR.values()) {
            var mat = textures.get(logic);
            if (mat == null) continue;

            var connFace = new CuboidFace(cull, tintIndex, "", faceUVs, Quadrant.R0);
            if (connFace.cullForDirection() == null) unculledFaces.add(face);

            connQuads[logic.ordinal()] = FaceBakery.bakeQuad(baker, offsets[0], offsets[1], connFace, mat, face, state, null, shadeDirectionOverride, emissivity);
        }
    }
}
