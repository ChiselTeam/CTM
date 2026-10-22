package io.github.chiselteam.ctm.client.unbaked;

import com.mojang.datafixers.util.Pair;
import io.github.chiselteam.ctm.client.model.CTMFace.Quadrant;
import com.mojang.serialization.MapCodec;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.api.strategy.CTMLogic;
import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;
import io.github.chiselteam.ctm.client.AbstractUnbakedConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.client.baked.TBSCTMBlockStateModel;
import io.github.chiselteam.ctm.impl.texture.CTMTextureSet;
import io.github.chiselteam.ctm.impl.texture.CTMFaceOrientation;
import io.github.chiselteam.ctm.client.model.CTMBakedModel;
import io.github.chiselteam.ctm.client.model.CTMModelVariant;
import io.github.chiselteam.ctm.client.model.CTMBakingContext;
import io.github.chiselteam.ctm.client.model.CTMFace;
import io.github.chiselteam.ctm.client.model.CTMFaceBakery;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import io.github.chiselteam.ctm.client.model.CTMUnbakedModel;
import org.joml.Vector3f;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TBSUnbakedCTMModel extends AbstractUnbakedConnectedTextureBlockStateModel {

    public TBSUnbakedCTMModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, ResourceLocation> textureSlots, CTMModelVariant.SimpleModelState modelState) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots, modelState);
    }

    public TBSUnbakedCTMModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, ResourceLocation> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots);
    }

    public TBSUnbakedCTMModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, ResourceLocation> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, eldritch, connectionPredicate, overlays, textureSlots);
    }

    public TBSUnbakedCTMModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity) {
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

        var bakedBase = bakeMaterial(baker, getMaterial(CTMTextureKeys.BASE));
        var bakedParticle = bakeMaterial(baker, getMaterial(CTMTextureKeys.PARTICLE));
        var topTextures = bakeStandardTextureSet(baker, Direction.UP);
        var bottomTextures = bakeStandardTextureSet(baker, Direction.DOWN);
        var sideTextures = bakeStandardTextureSet(baker, Direction.NORTH);
        topTextures.requireComplete(modelLocation + " (top)");
        bottomTextures.requireComplete(modelLocation + " (bottom)");
        sideTextures.requireComplete(modelLocation + " (side)");
        var bakedTop = bakeMaterial(baker, getMaterial("top"));
        if (bakedTop == null) bakedTop = bakedBase;
        var bakedBottom = bakeMaterial(baker, getMaterial("bottom"));
        if (bakedBottom == null) bakedBottom = bakedBase;
        var bakedSide = bakeMaterial(baker, getMaterial("side"));
        if (bakedSide == null) bakedSide = bakedBase;
        if (bakedParticle == null) bakedParticle = bakedSide != null ? bakedSide : sideTextures.get(CTMLogic.NONE);

        var baseQuads = new EnumMap<Direction, BakedQuad[]>(Direction.class);
        var connectedQuads = new EnumMap<Direction, BakedQuad[][]>(Direction.class);
        var unculledFaces = new HashSet<Direction>();

        var from = element.getFirst();
        var to = element.getSecond();
        var center = 8;

        for (var face : Direction.values()) {
            var cull = getCullface(face, from, to);
            var planeDirections = CTMFaceOrientation.localDirections(face);

            var baseQuadList = new ArrayList<BakedQuad>();
            var connQuads = new BakedQuad[4][CTMLogic.values().length];

            var faceTextures = switch (face) {
                case UP -> topTextures;
                case DOWN -> bottomTextures;
                default -> sideTextures;
            };

            var bakedFaceBase = switch (face) {
                case UP -> bakedTop;
                case DOWN -> bakedBottom;
                default -> bakedSide;
            };
            for (var c = 0; c < 4; c++) {
                var corner = face.getNormal().offset(planeDirections[c].getNormal()).offset(planeDirections[(c + 1) % 4].getNormal()).offset(1, 1, 1).multiply(8);

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

                if (bakedFaceBase != null) {
                    var baseFace = new CTMFace(cull, baseTintIndex, "", qUvs, Quadrant.R0);
                    var offsetFrom = new Vector3f(qFrom);
                    var offsetTo = new Vector3f(qTo);
                    baseQuadList.add(CTMFaceBakery.bakeQuad(baker, offsetFrom, offsetTo, baseFace, bakedFaceBase, face, state, null, shade, baseEmissivity));
                }

                for (var logic : CTMLogic.values()) {
                    var connFace = new CTMFace(cull, tintIndex, "", qUvs, Quadrant.R0);
                    if (connFace.cullForDirection() == null) {
                        unculledFaces.add(face);
                    }
                    connQuads[c][logic.ordinal()] = CTMFaceBakery.bakeQuad(baker, offsets[0], offsets[1],
                            connFace, faceTextures.get(logic), face, state, null, shade, emissivity);
                }
            }

            if (!baseQuadList.isEmpty()) {
                baseQuads.put(face, baseQuadList.toArray(new BakedQuad[0]));
            }
            connectedQuads.put(face, connQuads);
        }

        var bakedOverlays = bakeOverlays();
        return new TBSCTMBlockStateModel(remapDirections(connectedFaces, connectedQuads), remapDirections(unculledFaces, connectedQuads), renderOverlayOnAllFaces, remapQuadDirections(baseQuads), remapQuadDirections(connectedQuads), bakedParticle != null ? bakedParticle.sprite() : null, variant, connectionPredicate, bakedOverlays, remapRuleQuadDirections(bakeOverlayQuads(baker, bakedOverlays, from, to, state)), ambientOcclusion, CTMFaceOrientation.of(state));
    }

    private CTMTextureSet<CTMLogic> bakeStandardTextureSet(CTMBakingContext baker, Direction face) {
        var textures = new CTMTextureSet<>(CTMLogic.class);
        for (var logic : CTMLogic.values()) {
            textures.put(logic, bakeMaterial(baker, getMaterial(logic.getStandardTextureSlot(face))));
        }
        return textures;
    }
}
