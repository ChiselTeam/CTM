package io.github.chiselteam.ctm.client.unbaked;

import io.github.chiselteam.ctm.api.model.CTMOverlayRule;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.api.strategy.CTMLogic;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.CTMLogicHorizontal;
import io.github.chiselteam.ctm.api.strategy.CTMLogicVertical;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quadrant;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import io.github.chiselteam.ctm.client.AbstractUnbakedConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.client.baked.DirectionalCTMBlockStateModel;
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

public class DirectionalUnbakedCTMModel extends AbstractUnbakedConnectedTextureBlockStateModel {

    public DirectionalUnbakedCTMModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, eldritch, connectionPredicate, overlays, textureSlots);
    }

    public DirectionalUnbakedCTMModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity) {
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
        Material overlayTopMaterial = getMaterial(model, "overlay_top");
        Material overlayBottomMaterial = getMaterial(model, "overlay_bottom");
        Material overlaySideMaterial = getMaterial(model, "overlay_side");
        Material overlayHorizontalMaterial = getMaterial(model, "overlay_horizontal");
        Material overlayVerticalMaterial = getMaterial(model, "overlay_vertical");
        Material topMaterial = getMaterial(model, "top");
        Material bottomMaterial = getMaterial(model, "bottom");

        Material layer0Material = getMaterial(model, "layer0");
        Material layer1Material = getMaterial(model, "layer1");

        Material.Baked bakedBase = bakeMaterial(baker, baseMaterial != null ? baseMaterial : layer0Material, model);
        Material.Baked bakedOverlay = bakeMaterial(baker, overlayMaterial != null ? overlayMaterial : layer1Material, model);
        Material.Baked bakedParticle = bakeMaterial(baker, particleMaterial, model);
        if (bakedParticle == null) bakedParticle = (bakedBase != null ? bakedBase : bakedOverlay);

        Material.Baked bakedOverlayTop = bakeMaterial(baker, overlayTopMaterial, model);
        if (bakedOverlayTop == null) bakedOverlayTop = bakedOverlay;
        Material.Baked bakedOverlayBottom = bakeMaterial(baker, overlayBottomMaterial, model);
        if (bakedOverlayBottom == null) bakedOverlayBottom = bakedOverlay;
        Material.Baked bakedOverlaySide = bakeMaterial(baker, overlaySideMaterial, model);
        if (bakedOverlaySide == null) bakedOverlaySide = bakedOverlay;
        Material.Baked bakedOverlayHorizontal = bakeMaterial(baker, overlayHorizontalMaterial, model);
        Material.Baked bakedOverlayVertical = bakeMaterial(baker, overlayVerticalMaterial, model);
        Material.Baked bakedTop = bakeMaterial(baker, topMaterial, model);
        Material.Baked bakedBottom = bakeMaterial(baker, bottomMaterial, model);

        Map<Direction, BakedQuad[]> baseQuads = new EnumMap<>(Direction.class);
        Map<Direction, BakedQuad[]> horizontalQuads = new EnumMap<>(Direction.class);
        Map<Direction, BakedQuad[]> verticalQuads = new EnumMap<>(Direction.class);
        Set<Direction> unculledFaces = new HashSet<>();

        Vector3f from = element.getFirst();
        Vector3f to = element.getSecond();
        int center = 8;

        for (Direction face : Direction.values()) {
            Direction cull = getCullface(face, from, to);
            Direction[] planeDirections = CTMLogic.AXIS_PLANE_DIRECTIONS[face.getAxis().ordinal()];

            Material.Baked baseForFace = bakedBase;
            if (variant.kind().isCTMH()) {
                if (face == Direction.UP && bakedTop != null) baseForFace = bakedTop;
                else if (face == Direction.DOWN && bakedBottom != null) baseForFace = bakedBottom;
            }

            List<BakedQuad> baseQuadList = new ArrayList<>();
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

                if (baseForFace != null) {
                    CuboidFace.UVs qUvs = getRelativeUVs(face, qFrom, qTo);
                    CuboidFace baseFace = new CuboidFace(cull, baseTintIndex, "", CTMLogic.NONE.remapUVs(qUvs), Quadrant.R0);
                    Vector3f offsetFrom = new Vector3f(qFrom);
                    Vector3f offsetTo = new Vector3f(qTo);
                    baseQuadList.add(FaceBakery.bakeQuad(baker, offsetFrom, offsetTo, baseFace, baseForFace, face, state, null, true, baseEmissivity));
                }
            }
            if (!baseQuadList.isEmpty()) {
                baseQuads.put(face, baseQuadList.toArray(new BakedQuad[0]));
            }

            CuboidFace.UVs faceUvs = getRelativeUVs(face, from, to);

            Vector3f[] offsets = getOffsets(face, from, to);

            boolean isHorizontalDirectionalFace = (variant.kind().isBookshelfLike() || variant.kind().isCTMH()) && face.getAxis().isHorizontal() && bakedOverlayHorizontal != null;

            if (isHorizontalDirectionalFace) {
                BakedQuad[] quads = new BakedQuad[CTMLogicHorizontal.values().length];
                Vector3f qFrom = offsets[0];
                Vector3f qTo = offsets[1];
                if (variant.kind().isBookshelfLike() && bakedBase != null) {
                    float offset = 0.05f;
                    qFrom = new Vector3f(offsets[0]).add(face == Direction.WEST ? -offset : 0, face == Direction.DOWN ? -offset : 0, face == Direction.NORTH ? -offset : 0);
                    qTo = new Vector3f(offsets[1]).add(face == Direction.EAST ? offset : 0, face == Direction.UP ? offset : 0, face == Direction.SOUTH ? offset : 0);
                }
                for (CTMLogicHorizontal logic : CTMLogicHorizontal.values()) {
                    CuboidFace connFace = new CuboidFace(cull, tintIndex, "", logic.remapUVs(faceUvs), Quadrant.R0);
                    if (connFace.cullForDirection() == null) unculledFaces.add(face);
                    quads[logic.ordinal()] = FaceBakery.bakeQuad(baker, qFrom, qTo, connFace, bakedOverlayHorizontal, face, state, null, true, emissivity);
                }
                horizontalQuads.put(face, quads);
            }

            if (variant.kind().isCTMV()) {
                Material.Baked bakedOverlayV = switch (face) {
                    case UP -> bakedOverlayTop;
                    case DOWN -> bakedOverlayBottom;
                    default -> bakedOverlayVertical != null ? bakedOverlayVertical : bakedOverlaySide;
                };
                if (bakedOverlayV != null) {
                    BakedQuad[] quads = new BakedQuad[CTMLogicVertical.values().length];
                    for (CTMLogicVertical logic : CTMLogicVertical.values()) {
                        CuboidFace.UVs remappedUVs = face.getAxis().isHorizontal() ? logic.remapUVs(faceUvs) : faceUvs;
                        CuboidFace connFace = new CuboidFace(cull, tintIndex, "", remappedUVs, Quadrant.R0);
                        if (connFace.cullForDirection() == null) unculledFaces.add(face);
                        quads[logic.ordinal()] = FaceBakery.bakeQuad(baker, offsets[0], offsets[1], connFace, bakedOverlayV, face, state, null, true, emissivity);
                    }
                    verticalQuads.put(face, quads);
                }
            }
        }

        List<CTMOverlayRule> bakedOverlays = bakeOverlays(model);
        return new DirectionalCTMBlockStateModel(connectedFaces, unculledFaces, renderOverlayOnAllFaces, baseQuads, horizontalQuads, verticalQuads, bakedParticle != null ? bakedParticle.sprite() : null, variant, connectionPredicate, bakedOverlays, bakeOverlayQuads(baker, bakedOverlays, model, from, to, state));
    }
}
