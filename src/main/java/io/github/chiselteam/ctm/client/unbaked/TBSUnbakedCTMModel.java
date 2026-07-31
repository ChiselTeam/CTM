package io.github.chiselteam.ctm.client.unbaked;

import io.github.chiselteam.ctm.api.model.CTMOverlayRule;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.api.strategy.CTMLogic;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quadrant;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import io.github.chiselteam.ctm.client.AbstractUnbakedConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.client.baked.TBSCTMBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.Variant.SimpleModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.NeoForgeModelProperties;
import net.neoforged.neoforge.client.model.UnbakedElementsHelper;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class TBSUnbakedCTMModel extends AbstractUnbakedConnectedTextureBlockStateModel {

    public TBSUnbakedCTMModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, eldritch, connectionPredicate, overlays, textureSlots);
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
        ResolvedModel model = baker.getModel(modelLocation);
        ModelState state = SimpleModelState.DEFAULT.asModelState();
        Transformation rootTransform = model.getTopAdditionalProperties().getOrDefault(NeoForgeModelProperties.TRANSFORM, Transformation.IDENTITY);
        if (!rootTransform.isIdentity()) {
            state = UnbakedElementsHelper.composeRootTransformIntoModelState(state, rootTransform);
        }

        Material baseMaterial = getMaterial(model, "base_texture");
        Material overlayMaterial = getMaterial(model, "overlay_texture");
        Material particleMaterial = getMaterial(model, "particle");

        Material topMaterial = getMaterial(model, "top");
        Material bottomMaterial = getMaterial(model, "bottom");
        Material sideMaterial = getMaterial(model, "side");

        Material overlayTopMaterial = getMaterial(model, "overlay_top");
        Material overlayBottomMaterial = getMaterial(model, "overlay_bottom");
        Material overlaySideMaterial = getMaterial(model, "overlay_side");

        Material overlayTopConnectedMaterial = getMaterial(model, "overlay_top_connected");
        Material overlayBottomConnectedMaterial = getMaterial(model, "overlay_bottom_connected");
        Material overlaySideConnectedMaterial = getMaterial(model, "overlay_side_connected");
        Material overlayConnectedMaterial = getMaterial(model, "overlay_connected");

        Material layer0Material = getMaterial(model, "layer0");
        Material layer1Material = getMaterial(model, "layer1");

        Material.Baked bakedBase = bakeMaterial(baker, baseMaterial, model);
        Material.Baked bakedOverlay = bakeMaterial(baker, overlayMaterial, model);
        Material.Baked bakedParticle = bakeMaterial(baker, particleMaterial, model);

        Material.Baked bakedTop = bakeMaterial(baker, topMaterial, model);
        if (bakedTop == null) bakedTop = bakedBase;
        Material.Baked bakedBottom = bakeMaterial(baker, bottomMaterial, model);
        if (bakedBottom == null) bakedBottom = bakedBase;
        Material.Baked bakedSide = bakeMaterial(baker, sideMaterial, model);
        if (bakedSide == null) bakedSide = bakedBase;

        Material.Baked bakedOverlayTop = bakeMaterial(baker, overlayTopMaterial, model);
        if (bakedOverlayTop == null) bakedOverlayTop = bakedOverlay;
        Material.Baked bakedOverlayBottom = bakeMaterial(baker, overlayBottomMaterial, model);
        if (bakedOverlayBottom == null) bakedOverlayBottom = bakedOverlay;
        Material.Baked bakedOverlaySide = bakeMaterial(baker, overlaySideMaterial, model);
        if (bakedOverlaySide == null) bakedOverlaySide = bakedOverlay;

        Material.Baked bakedOverlayTopConnected = bakeMaterial(baker, overlayTopConnectedMaterial != null ? overlayTopConnectedMaterial : overlayConnectedMaterial, model);
        Material.Baked bakedOverlayBottomConnected = bakeMaterial(baker, overlayBottomConnectedMaterial != null ? overlayBottomConnectedMaterial : overlayConnectedMaterial, model);
        Material.Baked bakedOverlaySideConnected = bakeMaterial(baker, overlaySideConnectedMaterial != null ? overlaySideConnectedMaterial : overlayConnectedMaterial, model);

        if (bakedOverlayTop == null) bakedOverlayTop = bakedOverlayTopConnected;
        if (bakedOverlayBottom == null) bakedOverlayBottom = bakedOverlayBottomConnected;
        if (bakedOverlaySide == null) bakedOverlaySide = bakedOverlaySideConnected;

        if (bakedOverlayTopConnected == null) bakedOverlayTopConnected = bakedOverlayTop;
        if (bakedOverlayBottomConnected == null) bakedOverlayBottomConnected = bakedOverlayBottom;
        if (bakedOverlaySideConnected == null) bakedOverlaySideConnected = bakedOverlaySide;

        Map<Direction, BakedQuad[]> baseQuads = new EnumMap<>(Direction.class);
        Map<Direction, BakedQuad[][]> connectedQuads = new EnumMap<>(Direction.class);
        Set<Direction> unculledFaces = new HashSet<>();

        Vector3f from = element.getFirst();
        Vector3f to = element.getSecond();
        int center = 8;

        for (Direction face : Direction.values()) {
            Direction cull = getCullface(face, from, to);
            Direction[] planeDirections = CTMLogic.AXIS_PLANE_DIRECTIONS[face.getAxis().ordinal()];

            List<BakedQuad> baseQuadList = new ArrayList<>();
            BakedQuad[][] connQuads = new BakedQuad[4][CTMLogic.values().length];

            Material.Baked bakedFaceBase = switch (face) {
                case UP -> bakedTop;
                case DOWN -> bakedBottom;
                default -> bakedSide;
            };
            Material.Baked bakedFaceOverlay = switch (face) {
                case UP -> bakedOverlayTop;
                case DOWN -> bakedOverlayBottom;
                default -> bakedOverlaySide;
            };
            Material.Baked bakedFaceOverlayConnected = switch (face) {
                case UP -> bakedOverlayTopConnected;
                case DOWN -> bakedOverlayBottomConnected;
                default -> bakedOverlaySideConnected;
            };

            for (int c = 0; c < 4; c++) {
                Vec3i corner = face.getUnitVec3i().offset(planeDirections[c].getUnitVec3i()).offset(planeDirections[(c + 1) % 4].getUnitVec3i()).offset(1, 1, 1).multiply(8);

                Vector3f qFrom = new Vector3f(
                        Math.clamp(Math.min(center - (16 - to.x()), (float) corner.getX() + from.x()), 0, 16),
                        Math.clamp(Math.min(center - (16 - to.y()), (float) corner.getY() + from.y()), 0, 16),
                        Math.clamp(Math.min(center - (16 - to.z()), (float) corner.getZ() + from.z()), 0, 16)
                );
                Vector3f qTo = new Vector3f(
                        to.x() < center ? to.x() : Math.max(center, (float) corner.getX() - (16 - to.x())),
                        to.y() < center ? to.y() : Math.max(center, (float) corner.getY() - (16 - to.y())),
                        to.z() < center ? to.z() : Math.max(center, (float) corner.getZ() - (16 - to.z()))
                );

                CuboidFace.UVs qUvs = getRelativeUVs(face, qFrom, qTo);

                Vector3f[] offsets = getOffsets(face, qFrom, qTo);

                if (bakedFaceBase != null) {
                    CuboidFace baseFace = new CuboidFace(cull, baseTintIndex, "", CTMLogic.NONE.remapUVs(qUvs), Quadrant.R0);
                    Vector3f offsetFrom = new Vector3f(qFrom);
                    Vector3f offsetTo = new Vector3f(qTo);
                    baseQuadList.add(FaceBakery.bakeQuad(baker, offsetFrom, offsetTo, baseFace, bakedFaceBase, face, state, null, true, baseEmissivity));
                }

                if (bakedFaceOverlay != null && bakedFaceOverlayConnected != null) {
                    Material.Baked[] sprites = {bakedFaceOverlay, bakedFaceOverlayConnected};
                    for (CTMLogic logic : CTMLogic.values()) {
                        CuboidFace connFace = new CuboidFace(cull, tintIndex, "", logic.remapUVs(qUvs), Quadrant.R0);
                        if (connFace.cullForDirection() == null) {
                            unculledFaces.add(face);
                        }
                        connQuads[c][logic.ordinal()] = FaceBakery.bakeQuad(baker, offsets[0], offsets[1],
                                connFace, logic.chooseMaterial(sprites), face, state, null, true, emissivity);
                    }
                }
            }

            if (!baseQuadList.isEmpty()) {
                baseQuads.put(face, baseQuadList.toArray(new BakedQuad[0]));
            }
            connectedQuads.put(face, connQuads);
        }

        List<CTMOverlayRule> bakedOverlays = bakeOverlays(model);
        return new TBSCTMBlockStateModel(connectedFaces, unculledFaces, renderOverlayOnAllFaces, baseQuads, connectedQuads, bakedParticle != null ? bakedParticle.sprite() : null, variant, connectionPredicate, bakedOverlays, bakeOverlayQuads(baker, bakedOverlays, model, from, to, state));
    }
}
