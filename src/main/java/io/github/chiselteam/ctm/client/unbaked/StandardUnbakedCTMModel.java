package io.github.chiselteam.ctm.client.unbaked;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quadrant;
import com.mojang.serialization.MapCodec;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.api.strategy.CTMLogic;
import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;
import io.github.chiselteam.ctm.client.AbstractUnbakedConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.client.baked.StandardCTMBlockStateModel;
import io.github.chiselteam.ctm.impl.texture.CTMTextureSet;
import io.github.chiselteam.ctm.impl.texture.CTMFaceOrientation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class StandardUnbakedCTMModel extends AbstractUnbakedConnectedTextureBlockStateModel {

    public StandardUnbakedCTMModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, @Nullable Direction shadeDirectionOverride, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots, Variant.SimpleModelState modelState) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shadeDirectionOverride, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots, modelState);
    }

    public StandardUnbakedCTMModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, @Nullable Direction shadeDirectionOverride, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shadeDirectionOverride, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots);
    }

    public StandardUnbakedCTMModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, @Nullable Direction shadeDirectionOverride, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shadeDirectionOverride, eldritch, connectionPredicate, overlays, textureSlots);
    }

    public StandardUnbakedCTMModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity);
    }

    @Override
    public @NonNull MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return UnbakedConnectedTextureBlockStateModel.CODEC;
    }

    @Override
    public @NonNull BlockStateModel bake(@NonNull ModelBaker baker) {
        prepareBakery(baker);

        var bakedBase = bakeMaterial(baker, getMaterial(CTMTextureKeys.BASE));
        var bakedParticle = bakeMaterial(baker, getMaterial(CTMTextureKeys.PARTICLE));

        var standardTextures = bakeStandardTextureSet(baker);
        if (bakedParticle == null) {
            bakedParticle = bakedBase != null ? bakedBase : standardTextures.get(CTMLogic.NONE);
        }

        Map<Direction, BakedQuad[]> baseQuads = new EnumMap<>(Direction.class);
        Map<Direction, BakedQuad[][]> connectedQuads = new EnumMap<>(Direction.class);
        Set<Direction> unculledFaces = new HashSet<>();

        var from = element.getFirst();
        var to = element.getSecond();
        int center = 8;

        for (var face : Direction.values()) {
            var cull = getCullface(face, from, to);
            var planeDirections = CTMFaceOrientation.localDirections(face);

            var baseQuadList = new ArrayList<BakedQuad>();
            var connQuads = new BakedQuad[4][CTMLogic.values().length];

            for (int c = 0; c < 4; c++) {
                var corner = face.getUnitVec3i().offset(planeDirections[c].getUnitVec3i()).offset(planeDirections[(c + 1) % 4].getUnitVec3i()).offset(1, 1, 1).multiply(8);

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

                if (bakedBase != null) {
                    var baseFace = new CuboidFace(cull, baseTintIndex, "", qUvs, Quadrant.R0);
                    baseQuadList.add(FaceBakery.bakeQuad(baker, qFrom, qTo, baseFace, bakedBase, face, state, null, shadeDirectionOverride, baseEmissivity));
                }

                var offsets = getOffsets(face, qFrom, qTo);
                bakeStandaloneConnectedQuads(baker, state, face, cull, offsets[0], offsets[1], qUvs, standardTextures, connQuads[c], unculledFaces);
            }

            if (!baseQuadList.isEmpty()) baseQuads.put(face, baseQuadList.toArray(new BakedQuad[0]));
            connectedQuads.put(face, connQuads);
        }

        var bakedOverlays = bakeOverlays();
        return new StandardCTMBlockStateModel(remapDirections(connectedFaces, connectedQuads), remapDirections(unculledFaces, connectedQuads), renderOverlayOnAllFaces, remapQuadDirections(baseQuads), remapQuadDirections(connectedQuads), bakedParticle != null ? bakedParticle.sprite() : null, variant, connectionPredicate, bakedOverlays, remapRuleQuadDirections(bakeOverlayQuads(baker, bakedOverlays, from, to, state)), ambientOcclusion, CTMFaceOrientation.of(state));
    }

    private void bakeStandaloneConnectedQuads(ModelBaker baker, ModelState state, Direction face, Direction cull, Vector3f from, Vector3f to, CuboidFace.UVs uvs, CTMTextureSet<CTMLogic> textures, BakedQuad[] connQuads, Set<Direction> unculledFaces) {
        for(var logic : CTMLogic.values()) {
            var material = textures.get(logic);
            if(material == null) return;

            var connFace = new CuboidFace(cull, tintIndex, "", uvs, Quadrant.R0);
            if(connFace.cullForDirection() == null) unculledFaces.add(face);
            connQuads[logic.ordinal()] = FaceBakery.bakeQuad(baker, from, to, connFace, material, face, state, null, shadeDirectionOverride, emissivity);
        }
    }
}
