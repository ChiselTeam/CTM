package io.github.chiselteam.ctm.client.unbaked;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quadrant;
import com.mojang.serialization.MapCodec;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.api.strategy.CTMLogic;
import io.github.chiselteam.ctm.api.strategy.CTMLogicEdges;
import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;
import io.github.chiselteam.ctm.client.AbstractUnbakedConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.client.baked.EdgesCTMBlockStateModel;
import io.github.chiselteam.ctm.impl.texture.CTMTextureSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.ModelState;
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

public class EdgesUnbakedCTMModel extends AbstractUnbakedConnectedTextureBlockStateModel {

    public EdgesUnbakedCTMModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots, Variant.SimpleModelState modelState) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots, modelState);
    }

    public EdgesUnbakedCTMModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots);
    }

    public EdgesUnbakedCTMModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, eldritch, connectionPredicate, overlays, textureSlots);
    }

    @Override
    public @NonNull MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return UnbakedConnectedTextureBlockStateModel.CODEC;
    }

    @Override
    public @NonNull BlockStateModel bake(@NonNull ModelBaker baker) {
        prepareBakery(baker);

        var full = variant.kind().isEdgesFull();
        var standardTextures = full ? new CTMTextureSet<>(CTMLogic.class) : bakeStandardTextureSet(baker);
        var edgeTextures = full ? bakeEdgesTextureSet(baker) : new CTMTextureSet<>(CTMLogicEdges.class);
        var standalone = full ? edgeTextures.isComplete() : standardTextures.isComplete();
        var legacy = standalone ? new Material.Baked[2] : bakeLegacyMaterials(baker);
        var bakedBase = bakeMaterial(baker, getMaterial(CTMTextureKeys.BASE));
        var defaultOverlay = standalone ? (full ? edgeTextures.get(CTMLogicEdges.NONE) : standardTextures.get(CTMLogic.NONE)) : legacy[0];
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
            var planeDirections = CTMLogic.AXIS_PLANE_DIRECTIONS[face.getAxis().ordinal()];
            var baseQuadList = new ArrayList<BakedQuad>(4);
            var regularQuads = new BakedQuad[4][CTMLogic.values().length];

            for (var corner = 0; corner < 4; corner++) {
                var cornerVector = face.getUnitVec3i().offset(planeDirections[corner].getUnitVec3i()).offset(planeDirections[(corner + 1) % 4].getUnitVec3i()).offset(1, 1, 1).multiply(8);
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
                    var baseFace = new CuboidFace(cull, baseTintIndex, "", relativeUvs, Quadrant.R0);
                    baseQuadList.add(FaceBakery.bakeQuad(baker, quadFrom, quadTo, baseFace, bakedBase, face, state, null, shade, baseEmissivity));
                }
                if (full && standalone) continue;
                if (standalone) {
                    for (var logic : CTMLogic.values()) {
                        regularQuads[corner][logic.ordinal()] = bakeFace(baker, overlayBounds, cull, face, state, standardTextures.get(logic), relativeUvs);
                    }
                } else {
                    bakeLegacyRegularQuads(baker, overlayBounds, cull, face, relativeUvs, legacy, regularQuads[corner]);
                }
            }

            if (!baseQuadList.isEmpty()) baseQuads.put(face, baseQuadList.toArray(BakedQuad[]::new));
            regularConnectedQuads.put(face, regularQuads);
            var faceUvs = getRelativeUVs(face, from, to);
            var overlayBounds = getOverlayOffsets(face, from, to);
            var atlas = new BakedQuad[CTMLogicEdges.values().length];
            if (full || !standalone) {
                if (standalone) {
                    for (var logic : CTMLogicEdges.values()) {
                        atlas[logic.ordinal()] = bakeFace(baker, overlayBounds, cull, face, state, edgeTextures.get(logic), faceUvs);
                    }
                } else {
                    bakeLegacyAtlas(baker, overlayBounds, cull, face, faceUvs, legacy, atlas);
                }
            }
            fullAtlasQuads.put(face, atlas);
            var obscured = (!full || !standalone) && bakedObscured != null
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
        return new EdgesCTMBlockStateModel(remapDirections(connectedFaces, directionQuads), remapDirections(unculledFaces, directionQuads), renderOverlayOnAllFaces, remapQuadDirections(baseQuads), remapQuadDirections(regularConnectedQuads), remapQuadDirections(fullAtlasQuads), remapQuadDirections(obscuredQuads), bakedParticle.sprite(), variant, connectionPredicate, bakedOverlays, remapRuleQuadDirections(bakeOverlayQuads(baker, bakedOverlays, from, to, state)), ambientOcclusion);
    }

    private CTMTextureSet<CTMLogicEdges> bakeEdgesTextureSet(ModelBaker baker) {
        var textures = new CTMTextureSet<>(CTMLogicEdges.class);
        for (var logic : CTMLogicEdges.values()) {
            textures.put(logic, bakeMaterial(baker, getMaterial(logic.getTextureSlot())));
        }
        return textures;
    }

    @Deprecated(forRemoval = true, since = "26.1")
    private Material.Baked[] bakeLegacyMaterials(ModelBaker baker) {
        var overlay = bakeMaterial(baker, getMaterial("overlay_texture"));
        var connected = bakeMaterial(baker, getMaterial("overlay_connected"));
        if (overlay == null) overlay = connected;
        if (connected == null) connected = overlay;
        return new Material.Baked[]{overlay, connected};
    }

    @Deprecated(forRemoval = true, since = "26.1")
    private void bakeLegacyRegularQuads(ModelBaker baker, Vector3f[] bounds, Direction cull, Direction face, CuboidFace.UVs uvs, Material.Baked[] materials, BakedQuad[] quads) {
        if (materials[0] == null) return;
        for (var logic : CTMLogic.values()) {
            quads[logic.ordinal()] = bakeFace(baker, bounds, cull, face, state, logic.chooseMaterial(materials), logic.remapUVs(uvs));
        }
    }

    @Deprecated(forRemoval = true, since = "26.1")
    private void bakeLegacyAtlas(ModelBaker baker, Vector3f[] bounds, Direction cull, Direction face, CuboidFace.UVs uvs, Material.Baked[] materials, BakedQuad[] atlas) {
        if (materials[0] != null) atlas[0] = bakeFace(baker, bounds, cull, face, state, materials[0], uvs);
        if (materials[1] == null) return;
        for (var index = 1; index < atlas.length; index++) {
            atlas[index] = bakeFace(baker, bounds, cull, face, state, materials[1], remapToCell(uvs, index / 4, index % 4));
        }
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

    private BakedQuad bakeFace(ModelBaker baker, Vector3f[] bounds, Direction cull, Direction face, ModelState state, Material.Baked material, CuboidFace.UVs uvs) {
        var cuboidFace = new CuboidFace(cull, tintIndex, "", uvs, Quadrant.R0);
        return FaceBakery.bakeQuad(baker, bounds[0], bounds[1], cuboidFace, material, face, state, null, shade, emissivity);
    }

    @Deprecated(forRemoval = true, since = "26.1")
    private static CuboidFace.UVs remapToCell(CuboidFace.UVs uvs, int row, int column) {
        var cellU = column * 4.0F;
        var cellV = row * 4.0F;

        return new CuboidFace.UVs(cellU + uvs.minU() / 4.0F, cellV + uvs.minV() / 4.0F, cellU + uvs.maxU() / 4.0F, cellV + uvs.maxV() / 4.0F);
    }
}
