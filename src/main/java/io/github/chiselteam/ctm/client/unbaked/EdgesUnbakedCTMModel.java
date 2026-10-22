package io.github.chiselteam.ctm.client.unbaked;

import io.github.chiselteam.ctm.client.model.BakedTexture;
import com.mojang.datafixers.util.Pair;
import io.github.chiselteam.ctm.client.model.CTMFace.Quadrant;
import com.mojang.serialization.MapCodec;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.api.strategy.CTMLogic;
import io.github.chiselteam.ctm.api.strategy.CTMLogicEdges;
import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;
import io.github.chiselteam.ctm.client.AbstractUnbakedConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.client.baked.EdgesCTMBlockStateModel;
import io.github.chiselteam.ctm.impl.texture.CTMTextureSet;
import io.github.chiselteam.ctm.impl.texture.CTMFaceOrientation;
import io.github.chiselteam.ctm.client.model.CTMBakedModel;
import io.github.chiselteam.ctm.client.model.CTMModelState;
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

public class EdgesUnbakedCTMModel extends AbstractUnbakedConnectedTextureBlockStateModel {

    public EdgesUnbakedCTMModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, ResourceLocation> textureSlots, CTMModelVariant.SimpleModelState modelState) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots, modelState);
    }

    public EdgesUnbakedCTMModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, ResourceLocation> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots);
    }

    public EdgesUnbakedCTMModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, ResourceLocation> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, eldritch, connectionPredicate, overlays, textureSlots);
    }

    @Override
    public @NotNull MapCodec<? extends CTMUnbakedModel> codec() {
        return UnbakedConnectedTextureBlockStateModel.CODEC;
    }

    @Override
    public @NotNull CTMBakedModel bake(@NotNull CTMBakingContext baker) {
        baker = baker.withAmbientOcclusion(ambientOcclusion);
        prepareBakery(baker);

        var full = variant.kind().isEdgesFull();
        var standardTextures = full ? new CTMTextureSet<>(CTMLogic.class) : bakeStandardTextureSet(baker);
        var edgeTextures = full ? bakeEdgesTextureSet(baker) : new CTMTextureSet<>(CTMLogicEdges.class);
        if (full) edgeTextures.requireComplete(modelLocation.toString());
        else standardTextures.requireComplete(modelLocation.toString());
        var bakedBase = bakeMaterial(baker, getMaterial(CTMTextureKeys.BASE));
        var defaultOverlay = full ? edgeTextures.get(CTMLogicEdges.NONE) : standardTextures.get(CTMLogic.NONE);
        var bakedObscured = bakeMaterial(baker, getMaterial(CTMTextureKeys.OVERLAY_OBSCURED));
        if (bakedObscured == null) bakedObscured = defaultOverlay;
        var bakedParticle = bakeMaterial(baker, getMaterial(CTMTextureKeys.PARTICLE));
        if (bakedParticle == null) bakedParticle = bakedBase != null ? bakedBase : defaultOverlay;
        if (bakedParticle == null)
            throw new IllegalStateException("Edges CTM model has no usable particle, base, or overlay texture: %s".formatted(modelLocation));

        var baseQuads = new EnumMap<Direction, BakedQuad[]>(Direction.class);
        var regularConnectedQuads = new EnumMap<Direction, BakedQuad[][]>(Direction.class);
        var fullAtlasQuads = new EnumMap<Direction, BakedQuad[]>(Direction.class);
        var obscuredQuads = new EnumMap<Direction, BakedQuad[]>(Direction.class);
        var directionQuads = new EnumMap<Direction, BakedQuad[]>(Direction.class);
        var unculledFaces = new HashSet<Direction>();
        var from = element.getFirst();
        var to = element.getSecond();
        var center = 8;

        for (var face : Direction.values()) {
            var cull = getCullface(face, from, to);
            if (cull == null) unculledFaces.add(face);
            var planeDirections = CTMFaceOrientation.localDirections(face);
            var baseQuadList = new ArrayList<BakedQuad>(4);
            var regularQuads = new BakedQuad[4][CTMLogic.values().length];

            for (var corner = 0; corner < 4; corner++) {
                var cornerVector = face.getNormal().offset(planeDirections[corner].getNormal()).offset(planeDirections[(corner + 1) % 4].getNormal()).offset(1, 1, 1).multiply(8);
                var quadFrom = new Vector3f(
                        Math.clamp(Math.min(center - (16 - to.x()), (float) cornerVector.getX() + from.x()), 0, 16),
                        Math.clamp(Math.min(center - (16 - to.y()), (float) cornerVector.getY() + from.y()), 0, 16),
                        Math.clamp(Math.min(center - (16 - to.z()), (float) cornerVector.getZ() + from.z()), 0, 16)
                );
                var quadTo = new Vector3f(
                        to.x() < center ? to.x() : Math.max(center, (float) cornerVector.getX() - (16 - to.x())),
                        to.y() < center ? to.y() : Math.max(center, (float) cornerVector.getY() - (16 - to.y())),
                        to.z() < center ? to.z() : Math.max(center, (float) cornerVector.getZ() - (16 - to.z()))
                );
                var relativeUvs = getRelativeUVs(face, quadFrom, quadTo);
                var overlayBounds = getOverlayOffsets(face, quadFrom, quadTo);
                if (bakedBase != null) {
                    var baseFace = new CTMFace(cull, baseTintIndex, "", relativeUvs, Quadrant.R0);
                    baseQuadList.add(CTMFaceBakery.bakeQuad(baker, quadFrom, quadTo, baseFace, bakedBase, face, state, null, shade, baseEmissivity));
                }
                if (full) continue;

                for (var logic : CTMLogic.values()) {
                    regularQuads[corner][logic.ordinal()] = bakeFace(baker, overlayBounds, cull, face, state, standardTextures.get(logic), relativeUvs);
                }
            }

            if (!baseQuadList.isEmpty()) baseQuads.put(face, baseQuadList.toArray(BakedQuad[]::new));
            regularConnectedQuads.put(face, regularQuads);
            var faceUvs = getRelativeUVs(face, from, to);
            var overlayBounds = getOverlayOffsets(face, from, to);
            var atlas = new BakedQuad[CTMLogicEdges.values().length];
            if (full) {

                for (var logic : CTMLogicEdges.values()) {
                    atlas[logic.ordinal()] = bakeFace(baker, overlayBounds, cull, face, state, edgeTextures.get(logic), faceUvs);
                }
            }
            fullAtlasQuads.put(face, atlas);
            var obscured = !full && bakedObscured != null
                    ? new BakedQuad[]{bakeFace(baker, overlayBounds, cull, face, state, bakedObscured, faceUvs)}
                    : new BakedQuad[0];
            obscuredQuads.put(face, obscured);

            var directionSource = full ? atlas : Arrays.stream(regularQuads).flatMap(Arrays::stream).filter(Objects::nonNull).toArray(BakedQuad[]::new);
            if (Arrays.stream(directionSource).noneMatch(Objects::nonNull))
                directionSource = baseQuadList.toArray(BakedQuad[]::new);
            if (directionSource.length == 0) directionSource = obscured;
            directionQuads.put(face, directionSource);
        }

        var bakedOverlays = bakeOverlays();
        return new EdgesCTMBlockStateModel(remapDirections(connectedFaces, directionQuads), remapDirections(unculledFaces, directionQuads), renderOverlayOnAllFaces, remapQuadDirections(baseQuads), remapQuadDirections(regularConnectedQuads), remapQuadDirections(fullAtlasQuads), remapQuadDirections(obscuredQuads), bakedParticle.sprite(), variant, connectionPredicate, bakedOverlays, remapRuleQuadDirections(bakeOverlayQuads(baker, bakedOverlays, from, to, state)), ambientOcclusion, CTMFaceOrientation.of(state));
    }

    private CTMTextureSet<CTMLogicEdges> bakeEdgesTextureSet(CTMBakingContext baker) {
        var textures = new CTMTextureSet<>(CTMLogicEdges.class);
        for (var logic : CTMLogicEdges.values()) {
            textures.put(logic, bakeMaterial(baker, getMaterial(logic.getTextureSlot())));
        }
        return textures;
    }

    private static Vector3f[] getOverlayOffsets(Direction face, Vector3f from, Vector3f to) {
        var offset = 0.01F;
        var offsetFrom = new Vector3f(from);
        var offsetTo = new Vector3f(to);

        switch (face) {
            case DOWN -> {
                offsetFrom.y -= offset;
                offsetTo.y -= offset;
            }
            case UP -> {
                offsetFrom.y += offset;
                offsetTo.y += offset;
            }
            case NORTH -> {
                offsetFrom.z -= offset;
                offsetTo.z -= offset;
            }
            case SOUTH -> {
                offsetFrom.z += offset;
                offsetTo.z += offset;
            }
            case WEST -> {
                offsetFrom.x -= offset;
                offsetTo.x -= offset;
            }
            case EAST -> {
                offsetFrom.x += offset;
                offsetTo.x += offset;
            }
        }

        return new Vector3f[]{offsetFrom, offsetTo};
    }

    private BakedQuad bakeFace(CTMBakingContext baker, Vector3f[] bounds, Direction cull, Direction face, CTMModelState state, BakedTexture material, CTMFace.UVs uvs) {
        var cuboidFace = new CTMFace(cull, tintIndex, "", uvs, Quadrant.R0);
        return CTMFaceBakery.bakeQuad(baker, bounds[0], bounds[1], cuboidFace, material, face, state, null, shade, emissivity);
    }
}
