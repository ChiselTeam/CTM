package io.github.chiselteam.ctm.client.unbaked;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quadrant;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import io.github.chiselteam.ctm.api.model.CTMOverlayRule;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.api.strategy.CTMLogic;
import io.github.chiselteam.ctm.client.AbstractUnbakedConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.client.baked.EdgesCTMBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.Variant.SimpleModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.NeoForgeModelProperties;
import net.neoforged.neoforge.client.model.UnbakedElementsHelper;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EdgesUnbakedCTMModel extends AbstractUnbakedConnectedTextureBlockStateModel {

    public EdgesUnbakedCTMModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, eldritch, connectionPredicate, overlays, textureSlots);
    }

    @Override
    public @NonNull MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return UnbakedConnectedTextureBlockStateModel.CODEC;
    }

    @Override
    public @NonNull BlockStateModel bake(@NonNull ModelBaker baker) {
        ResolvedModel model = baker.getModel(modelLocation);

        ModelState state = SimpleModelState.DEFAULT.asModelState();

        Transformation rootTransform = model.getTopAdditionalProperties()
                .getOrDefault(
                        NeoForgeModelProperties.TRANSFORM,
                        Transformation.IDENTITY
                );

        if (!rootTransform.isIdentity()) {
            state = UnbakedElementsHelper.composeRootTransformIntoModelState(state, rootTransform);
        }

        Material.Baked bakedBase = bakeMaterial(baker, getMaterial(model, "base_texture"), model);
        Material.Baked bakedOverlay = bakeMaterial(baker, getMaterial(model, "overlay_texture"), model);
        Material.Baked bakedConnected = bakeMaterial(baker, getMaterial(model, "overlay_connected"), model);
        Material.Baked bakedObscured = bakeMaterial(baker, getMaterial(model, "overlay_obscured"), model);

        /*
         * Regular EDGES requires an unconnected and connected texture.
         * These fallbacks let simpler definitions reuse one texture.
         */
        if (bakedOverlay == null) bakedOverlay = bakedConnected;
        if (bakedConnected == null) bakedConnected = bakedOverlay;

        /*
         * The original EDGES implementation uses a third full-face texture
         * when the face is obscured. Falling back to overlay_texture keeps
         * overlay_obscured optional.
         */
        if (bakedObscured == null) bakedObscured = bakedOverlay;

        Material.Baked bakedParticle = bakeMaterial(baker, getMaterial(model, "particle"), model);

        if (bakedParticle == null) {
            bakedParticle = bakedBase != null ? bakedBase : bakedOverlay;
        }

        if (bakedParticle == null)
            throw new IllegalStateException("Edges CTM model has no usable particle, base, or overlay texture: %s".formatted(modelLocation));

        Map<Direction, BakedQuad[]> baseQuads = new EnumMap<>(Direction.class);
        Map<Direction, BakedQuad[][]> regularConnectedQuads = new EnumMap<>(Direction.class);
        Map<Direction, BakedQuad[]> fullAtlasQuads = new EnumMap<>(Direction.class);
        Map<Direction, BakedQuad[]> obscuredQuads = new EnumMap<>(Direction.class);
        Set<Direction> unculledFaces = new HashSet<>();

        Vector3f from = element.getFirst();
        Vector3f to = element.getSecond();

        int center = 8;

        for (Direction face : Direction.values()) {
            Direction cull = getCullface(face, from, to);
            if (cull == null) unculledFaces.add(face);

            Direction[] planeDirections = CTMLogic.AXIS_PLANE_DIRECTIONS[face.getAxis().ordinal()];
            List<BakedQuad> baseQuadList = new ArrayList<>(4);
            BakedQuad[][] regularQuads = new BakedQuad[4][CTMLogic.values().length];

            /*
             * Bake the four quadrants needed by regular EDGES.
             */
            for (int corner = 0; corner < 4; corner++) {
                Vec3i cornerVector = face.getUnitVec3i()
                        .offset(
                                planeDirections[corner].getUnitVec3i()
                        )
                        .offset(
                                planeDirections[
                                        (corner + 1) % 4
                                        ].getUnitVec3i()
                        )
                        .offset(1, 1, 1)
                        .multiply(8);

                Vector3f quadFrom = new Vector3f(
                        Math.clamp(
                                Math.min(
                                        center - (16 - to.x()),
                                        (float) cornerVector.getX()
                                                + from.x()
                                ),
                                0,
                                16
                        ),
                        Math.clamp(
                                Math.min(
                                        center - (16 - to.y()),
                                        (float) cornerVector.getY()
                                                + from.y()
                                ),
                                0,
                                16
                        ),
                        Math.clamp(
                                Math.min(
                                        center - (16 - to.z()),
                                        (float) cornerVector.getZ()
                                                + from.z()
                                ),
                                0,
                                16
                        )
                );

                Vector3f quadTo = new Vector3f(
                        to.x() < center
                                ? to.x()
                                : Math.max(
                                center,
                                (float) cornerVector.getX()
                                        - (16 - to.x())
                        ),
                        to.y() < center
                                ? to.y()
                                : Math.max(
                                center,
                                (float) cornerVector.getY()
                                        - (16 - to.y())
                        ),
                        to.z() < center
                                ? to.z()
                                : Math.max(
                                center,
                                (float) cornerVector.getZ()
                                        - (16 - to.z())
                        )
                );

                CuboidFace.UVs relativeUvs = getRelativeUVs(face, quadFrom, quadTo);
                Vector3f[] overlayBounds = getOverlayOffsets(face, quadFrom, quadTo);

                if (bakedBase != null) {
                    CuboidFace baseFace = new CuboidFace(cull, baseTintIndex, "", relativeUvs, Quadrant.R0);
                    baseQuadList.add(FaceBakery.bakeQuad(baker, quadFrom, quadTo, baseFace, bakedBase, face, state, null, true, baseEmissivity));
                }

                if (bakedOverlay != null) {
                    Material.Baked[] materials = {
                            bakedOverlay,
                            bakedConnected
                    };

                    for (CTMLogic logic : CTMLogic.values()) {
                        CuboidFace overlayFace = new CuboidFace(cull, tintIndex, "", logic.remapUVs(relativeUvs), Quadrant.R0);
                        regularQuads[corner][logic.ordinal()] = FaceBakery.bakeQuad(baker, overlayBounds[0], overlayBounds[1], overlayFace, logic.chooseMaterial(materials), face, state, null, true, emissivity);
                    }
                }
            }

            if (!baseQuadList.isEmpty()) {
                baseQuads.put(face, baseQuadList.toArray(BakedQuad[]::new));
            }

            regularConnectedQuads.put(face, regularQuads);

            /*
             * EDGES_FULL uses one complete face quad. Each array entry
             * contains the same geometry with UVs mapped to one 4x4 cell.
             */
            CuboidFace.UVs faceUvs = getRelativeUVs(face, from, to);
            Vector3f[] overlayBounds = getOverlayOffsets(face, from, to);

            BakedQuad[] atlas = new BakedQuad[16];

            /*
             * Cell [0][0] is used as the default/no-connection texture.
             * It comes from overlay_texture rather than overlay_connected.
             */
            if (bakedOverlay != null) {
                atlas[0] = bakeFace(baker, overlayBounds, cull, face, state, bakedOverlay, faceUvs);
            }

            if (bakedConnected != null) {
                for (int row = 0; row < 4; row++) {
                    for (int column = 0; column < 4; column++) {
                        int index = row * 4 + column;

                        if (index == 0) continue;

                        atlas[index] = bakeFace(baker, overlayBounds, cull, face, state, bakedConnected, remapToCell(faceUvs, row, column));
                    }
                }
            }

            fullAtlasQuads.put(face, atlas);

            /*
             * Regular EDGES uses this full-face quad when a matching block
             * directly obscures the current face.
             */
            if (bakedObscured != null) {
                obscuredQuads.put(face, new BakedQuad[]{
                        bakeFace(baker, overlayBounds, cull, face, state, bakedObscured, faceUvs)
                });
            } else {
                obscuredQuads.put(face, new BakedQuad[0]);
            }
        }

        List<CTMOverlayRule> bakedOverlays = bakeOverlays(model);

        return new EdgesCTMBlockStateModel(connectedFaces, unculledFaces, renderOverlayOnAllFaces, baseQuads, regularConnectedQuads, fullAtlasQuads, obscuredQuads, bakedParticle.sprite(), variant, connectionPredicate, bakedOverlays, bakeOverlayQuads(baker, bakedOverlays, model, from, to, state));
    }


    private static Vector3f[] getOverlayOffsets(Direction face, Vector3f from, Vector3f to) {
        float offset = 0.01F;
        Vector3f offsetFrom = new Vector3f(from);
        Vector3f offsetTo = new Vector3f(to);

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
        CuboidFace cuboidFace = new CuboidFace(cull, tintIndex, "", uvs, Quadrant.R0);

        return FaceBakery.bakeQuad(baker, bounds[0], bounds[1], cuboidFace, material, face, state, null, true, emissivity);
    }

    /**
     * Maps the face's normal 0-16 UV range into one cell of a 4x4 atlas.
     *
     * <p>Each cell occupies four texture pixels:
     *
     * <pre>
     * column 0 = U 0-4
     * column 1 = U 4-8
     * column 2 = U 8-12
     * column 3 = U 12-16
     * </pre>
     */
    private static CuboidFace.UVs remapToCell(CuboidFace.UVs uvs, int row, int column) {
        float cellU = column * 4.0F;
        float cellV = row * 4.0F;

        return new CuboidFace.UVs(
                cellU + uvs.minU() / 4.0F,
                cellV + uvs.minV() / 4.0F,
                cellU + uvs.maxU() / 4.0F,
                cellV + uvs.maxV() / 4.0F
        );
    }
}
