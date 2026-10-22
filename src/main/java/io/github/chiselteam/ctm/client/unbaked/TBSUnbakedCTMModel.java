package io.github.chiselteam.ctm.client.unbaked;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quadrant;
import com.mojang.serialization.MapCodec;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.api.strategy.CTMLogic;
import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;
import io.github.chiselteam.ctm.client.AbstractUnbakedConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.client.baked.TBSCTMBlockStateModel;
import io.github.chiselteam.ctm.impl.texture.CTMTextureSet;
import io.github.chiselteam.ctm.impl.texture.CTMFaceOrientation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
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

import java.util.*;

public class TBSUnbakedCTMModel extends AbstractUnbakedConnectedTextureBlockStateModel {

    public TBSUnbakedCTMModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots, Variant.SimpleModelState modelState) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots, modelState);
    }

    public TBSUnbakedCTMModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots);
    }

    public TBSUnbakedCTMModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, eldritch, connectionPredicate, overlays, textureSlots);
    }

    public TBSUnbakedCTMModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity);
    }

    @Override
    public @NonNull MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return UnbakedConnectedTextureBlockStateModel.CODEC;
    }

    @Override
    public @NonNull BlockStateModel bake(@NonNull ModelBaker baker) {
        prepareBakery(baker);

        var baseMaterial = getMaterial(CTMTextureKeys.BASE);
        var particleMaterial = getMaterial(CTMTextureKeys.PARTICLE);

        var topTextures = bakeStandardTextureSet(baker, Direction.UP);
        var bottomTextures = bakeStandardTextureSet(baker, Direction.DOWN);
        var sideTextures = bakeStandardTextureSet(baker, Direction.NORTH);

        var topMaterial = getMaterial("top");
        var bottomMaterial = getMaterial("bottom");
        var sideMaterial = getMaterial("side");

        var bakedBase = bakeMaterial(baker, baseMaterial);
        var bakedParticle = bakeMaterial(baker, particleMaterial);

        var bakedTop = bakeMaterial(baker, topMaterial);
        if (bakedTop == null) bakedTop = bakedBase;
        var bakedBottom = bakeMaterial(baker, bottomMaterial);
        if (bakedBottom == null) bakedBottom = bakedBase;
        var bakedSide = bakeMaterial(baker, sideMaterial);
        if (bakedSide == null) bakedSide = bakedBase;

        if (bakedParticle == null) {
            if (bakedSide != null) bakedParticle = bakedSide;
            else if (sideTextures.isComplete()) bakedParticle = sideTextures.get(CTMLogic.NONE);
            else if (bakedTop != null) bakedParticle = bakedTop;
            else if (topTextures.isComplete()) bakedParticle = topTextures.get(CTMLogic.NONE);
            else if (bakedBottom != null) bakedParticle = bakedBottom;
            else if (bottomTextures.isComplete()) bakedParticle = bottomTextures.get(CTMLogic.NONE);
        }

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

                var offsets = getOffsets(face, qFrom, qTo);

                if (bakedFaceBase != null) {
                    var baseFace = new CuboidFace(cull, baseTintIndex, "", qUvs, Quadrant.R0);
                    var offsetFrom = new Vector3f(qFrom);
                    var offsetTo = new Vector3f(qTo);
                    baseQuadList.add(FaceBakery.bakeQuad(baker, offsetFrom, offsetTo, baseFace, bakedFaceBase, face, state, null, shade, baseEmissivity));
                }

                if (faceTextures.isComplete()) {
                    for (var logic : CTMLogic.values()) {
                        var connFace = new CuboidFace(cull, tintIndex, "", qUvs, Quadrant.R0);
                        if (connFace.cullForDirection() == null) {
                            unculledFaces.add(face);
                        }
                        connQuads[c][logic.ordinal()] = FaceBakery.bakeQuad(baker, offsets[0], offsets[1],
                                connFace, faceTextures.get(logic), face, state, null, shade, emissivity);
                    }
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

    private CTMTextureSet<CTMLogic> bakeStandardTextureSet(ModelBaker baker, Direction face) {
        var textures = new CTMTextureSet<>(CTMLogic.class);
        for (var logic : CTMLogic.values()) {
            textures.put(logic, bakeMaterial(baker, getMaterial(logic.getStandardTextureSlot(face))));
        }
        return textures;
    }
}
