package io.github.chiselteam.ctm.client.unbaked;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quadrant;
import com.mojang.serialization.MapCodec;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.*;
import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;
import io.github.chiselteam.ctm.client.AbstractUnbakedConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.client.baked.MultiblockCTMBlockStateModel;
import io.github.chiselteam.ctm.impl.texture.CTMTextureSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
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

public class MultiblockUnbakedCTMModel extends AbstractUnbakedConnectedTextureBlockStateModel {

    public MultiblockUnbakedCTMModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots, Variant.SimpleModelState modelState) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots, modelState);
    }

    public MultiblockUnbakedCTMModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots);
    }

    public MultiblockUnbakedCTMModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, eldritch, connectionPredicate, overlays, textureSlots);
    }

    public MultiblockUnbakedCTMModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity);
    }

    @Override
    public @NonNull MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return UnbakedConnectedTextureBlockStateModel.CODEC;
    }

    @Override
    public @NonNull BlockStateModel bake(@NonNull ModelBaker baker) {
        prepareBakery(baker);

        var baseMaterial = getMaterial("base_texture");
        if (baseMaterial == null) baseMaterial = getMaterial("layer0");

        var bakedBase = bakeMaterial(baker, baseMaterial);
        var bakedParticle = bakeMaterial(baker, getMaterial("particle"));
        if (bakedParticle == null) bakedParticle = bakedBase;

        var textures2x2 = bake2x2Textures(baker, model);
        var textures3x3 = bake3x3Textures(baker, model);
        var textures4x4 = bake4x4Textures(baker, model);

        var standalone2x2 = textures2x2.isComplete();
        var standalone3x3 = textures3x3.isComplete();
        var standalone4x4 = textures4x4.isComplete();

        Material.Baked legacy2x2 = null;
        Material.Baked legacy3x3 = null;
        Material.Baked legacy4x4 = null;

        if (!standalone2x2) legacy2x2 = bakeLegacyTexture(baker, model, "overlay_2x2");
        if (!standalone3x3) legacy3x3 = bakeLegacyTexture(baker, model, "overlay_3x3");
        if (!standalone4x4) legacy4x4 = bakeLegacyTexture(baker, model, "overlay_4x4");

        Map<Direction, BakedQuad[]> baseQuads = new EnumMap<>(Direction.class);
        Map<Direction, BakedQuad[]> mb2x2Quads = new EnumMap<>(Direction.class);
        Map<Direction, BakedQuad[]> mb3x3Quads = new EnumMap<>(Direction.class);
        Map<Direction, BakedQuad[]> mb4x4Quads = new EnumMap<>(Direction.class);
        Set<Direction> unculledFaces = new HashSet<>();

        var from = element.getFirst();
        var to = element.getSecond();
        int center = 8;

        for (var face : Direction.values()) {
            var cull = getCullface(face, from, to);
            var planeDirections = CTMLogic.AXIS_PLANE_DIRECTIONS[face.getAxis().ordinal()];
            var baseQuadsList = new ArrayList<BakedQuad>();

            if (bakedBase != null) {
                if (variant.waterOffset()) {
                    var faceUVs = getRelativeUVs(face, from, to);
                    var baseFace = new CuboidFace(cull, baseTintIndex, "", faceUVs, Quadrant.R0);
                    var inset = 0.005F;
                    var qFrom = new Vector3f(from);
                    var qTo = new Vector3f(to);

                    switch (face) {
                        case DOWN -> {
                            qFrom.y += inset;
                            qTo.y += inset;
                        }
                        case UP -> {
                            qFrom.y -= inset;
                            qTo.y -= inset;
                        }
                        case NORTH -> {
                            qFrom.z += inset;
                            qTo.z += inset;
                        }
                        case SOUTH -> {
                            qFrom.z -= inset;
                            qTo.z -= inset;
                        }
                        case WEST -> {
                            qFrom.x += inset;
                            qTo.x += inset;
                        }
                        case EAST -> {
                            qFrom.x -= inset;
                            qTo.x -= inset;
                        }
                    }

                    baseQuadsList.add(FaceBakery.bakeQuad(baker, qFrom, qTo, baseFace, bakedBase, face, state, null, shade, baseEmissivity));
                } else {
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

                        CuboidFace.UVs qUvs = getRelativeUVs(face, qFrom, qTo);
                        CuboidFace baseFace = new CuboidFace(cull, baseTintIndex, "", qUvs, Quadrant.R0);

                        baseQuadsList.add(FaceBakery.bakeQuad(baker, qFrom, qTo, baseFace, bakedBase, face, state, null, shade, baseEmissivity));
                    }
                }
            }

            if (!baseQuadsList.isEmpty()) baseQuads.put(face, baseQuadsList.toArray(new BakedQuad[0]));
            var faceUvs = getRelativeUVs(face, from, to);
            var offsets = getOffsets(face, from, to);

            if (standalone2x2)
                bakeStandaloneMultiblock(baker, state, face, cull, faceUvs, mb2x2Quads, unculledFaces, textures2x2, offsets);
            if (legacy2x2 != null)
                bakeLegacy2x2(baker, state, face, cull, faceUvs, mb2x2Quads, unculledFaces, legacy2x2, offsets);

            if (standalone3x3)
                bakeStandaloneMultiblock(baker, state, face, cull, faceUvs, mb3x3Quads, unculledFaces, textures3x3, offsets);
            if (legacy3x3 != null)
                bakeLegacy3x3(baker, state, face, cull, faceUvs, mb3x3Quads, unculledFaces, legacy3x3, offsets);

            if (standalone4x4)
                bakeStandaloneMultiblock(baker, state, face, cull, faceUvs, mb4x4Quads, unculledFaces, textures4x4, offsets);
            if (legacy4x4 != null)
                bakeLegacy4x4(baker, state, face, cull, faceUvs, mb4x4Quads, unculledFaces, legacy4x4, offsets);
        }

        var bakedOverlays = bakeOverlays();
        var directionSource = switch (variant.kind()) {
            case MULTIBLOCK_3X3 -> mb3x3Quads;
            case MULTIBLOCK_4X4 -> mb4x4Quads;
            default -> mb2x2Quads;
        };

        return new MultiblockCTMBlockStateModel(
                remapDirections(connectedFaces, directionSource),
                remapDirections(unculledFaces, directionSource),
                renderOverlayOnAllFaces,
                remapQuadDirections(baseQuads),
                remapQuadDirections(mb2x2Quads),
                remapQuadDirections(mb3x3Quads),
                remapQuadDirections(mb4x4Quads),
                bakedParticle != null ? bakedParticle.sprite() : null,
                variant,
                connectionPredicate,
                bakedOverlays,
                remapRuleQuadDirections(bakeOverlayQuads(baker, bakedOverlays, from, to, state)),
                ambientOcclusion);
    }

    private CTMTextureSet<CTMLogic2x2> bake2x2Textures(ModelBaker baker, ResolvedModel model) {
        var textures = new CTMTextureSet<>(CTMLogic2x2.class);

        textures.put(CTMLogic2x2.TOP_LEFT, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_2X2_TOP_LEFT)));
        textures.put(CTMLogic2x2.TOP_RIGHT, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_2X2_TOP_RIGHT)));
        textures.put(CTMLogic2x2.BOTTOM_LEFT, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_2X2_BOTTOM_LEFT)));
        textures.put(CTMLogic2x2.BOTTOM_RIGHT, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_2X2_BOTTOM_RIGHT)));

        return textures;
    }

    private CTMTextureSet<CTMLogic3x3> bake3x3Textures(ModelBaker baker, ResolvedModel model) {
        var textures = new CTMTextureSet<>(CTMLogic3x3.class);

        textures.put(CTMLogic3x3.TOP_LEFT, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_3X3_TOP_LEFT)));
        textures.put(CTMLogic3x3.TOP_CENTER, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_3X3_TOP_CENTER)));
        textures.put(CTMLogic3x3.TOP_RIGHT, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_3X3_TOP_RIGHT)));

        textures.put(CTMLogic3x3.MID_LEFT, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_3X3_CENTER_LEFT)));
        textures.put(CTMLogic3x3.MID_CENTER, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_3X3_CENTER)));
        textures.put(CTMLogic3x3.MID_RIGHT, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_3X3_CENTER_RIGHT)));

        textures.put(CTMLogic3x3.BOTTOM_LEFT, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_3X3_BOTTOM_LEFT)));
        textures.put(CTMLogic3x3.BOTTOM_CENTER, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_3X3_BOTTOM_CENTER)));
        textures.put(CTMLogic3x3.BOTTOM_RIGHT, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_3X3_BOTTOM_RIGHT)));

        return textures;
    }

    private CTMTextureSet<CTMLogic4x4> bake4x4Textures(ModelBaker baker, ResolvedModel model) {
        var textures = new CTMTextureSet<>(CTMLogic4x4.class);

        textures.put(CTMLogic4x4.ROW0_COL0, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_4X4_ROW_0_COLUMN_0)));
        textures.put(CTMLogic4x4.ROW0_COL1, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_4X4_ROW_0_COLUMN_1)));
        textures.put(CTMLogic4x4.ROW0_COL2, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_4X4_ROW_0_COLUMN_2)));
        textures.put(CTMLogic4x4.ROW0_COL3, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_4X4_ROW_0_COLUMN_3)));

        textures.put(CTMLogic4x4.ROW1_COL0, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_4X4_ROW_1_COLUMN_0)));
        textures.put(CTMLogic4x4.ROW1_COL1, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_4X4_ROW_1_COLUMN_1)));
        textures.put(CTMLogic4x4.ROW1_COL2, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_4X4_ROW_1_COLUMN_2)));
        textures.put(CTMLogic4x4.ROW1_COL3, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_4X4_ROW_1_COLUMN_3)));

        textures.put(CTMLogic4x4.ROW2_COL0, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_4X4_ROW_2_COLUMN_0)));
        textures.put(CTMLogic4x4.ROW2_COL1, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_4X4_ROW_2_COLUMN_1)));
        textures.put(CTMLogic4x4.ROW2_COL2, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_4X4_ROW_2_COLUMN_2)));
        textures.put(CTMLogic4x4.ROW2_COL3, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_4X4_ROW_2_COLUMN_3)));

        textures.put(CTMLogic4x4.ROW3_COL0, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_4X4_ROW_3_COLUMN_0)));
        textures.put(CTMLogic4x4.ROW3_COL1, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_4X4_ROW_3_COLUMN_1)));
        textures.put(CTMLogic4x4.ROW3_COL2, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_4X4_ROW_3_COLUMN_2)));
        textures.put(CTMLogic4x4.ROW3_COL3, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_4X4_ROW_3_COLUMN_3)));

        return textures;
    }

    private <T extends Enum<T>> void bakeStandaloneMultiblock(ModelBaker baker, ModelState state, Direction face, Direction cull, CuboidFace.UVs uvs, Map<Direction, BakedQuad[]> dest, Set<Direction> unculled, CTMTextureSet<T> textures, Vector3f[] offsets) {
        var values = textures.getType().getEnumConstants();
        var quads = new BakedQuad[values.length];

        for (T logic : values) {
            var material = textures.get(logic);
            if (material == null) continue;

            var faceDef = new CuboidFace(cull, tintIndex, "", uvs, Quadrant.R0);
            if (faceDef.cullForDirection() == null) unculled.add(face);

            quads[logic.ordinal()] = FaceBakery.bakeQuad(baker, offsets[0], offsets[1], faceDef, material, face, state, null, shade, emissivity);
        }

        dest.put(face, quads);
    }

    @Deprecated(forRemoval = true, since = "26.1")
    private Material.Baked bakeLegacyTexture(ModelBaker baker, ResolvedModel model, String textureKey) {
        return bakeMaterial(baker, getMaterial(textureKey));
    }

    @Deprecated(forRemoval = true, since = "26.1")
    private void bakeLegacy2x2(ModelBaker baker, ModelState state, Direction face, Direction cull, CuboidFace.UVs uvs, Map<Direction, BakedQuad[]> dest, Set<Direction> unculled, Material.Baked material, Vector3f[] offsets) {
        var quads = new BakedQuad[CTMLogic2x2.values().length];

        for (var logic : CTMLogic2x2.values()) {
            var faceDef = new CuboidFace(cull, tintIndex, "", logic.remapUVs(uvs), Quadrant.R0);
            if (faceDef.cullForDirection() == null) unculled.add(face);

            quads[logic.ordinal()] = FaceBakery.bakeQuad(baker, offsets[0], offsets[1], faceDef, material, face, state, null, shade, emissivity);
        }

        dest.put(face, quads);
    }

    @Deprecated(forRemoval = true, since = "26.1")
    private void bakeLegacy3x3(ModelBaker baker, ModelState state, Direction face, Direction cull, CuboidFace.UVs uvs, Map<Direction, BakedQuad[]> dest, Set<Direction> unculled, Material.Baked material, Vector3f[] offsets) {
        var quads = new BakedQuad[CTMLogic3x3.values().length];

        for (var logic : CTMLogic3x3.values()) {
            var faceDef = new CuboidFace(cull, tintIndex, "", logic.remapUVs(uvs), Quadrant.R0);
            if (faceDef.cullForDirection() == null) unculled.add(face);

            quads[logic.ordinal()] = FaceBakery.bakeQuad(baker, offsets[0], offsets[1], faceDef, material, face, state, null, shade, emissivity);
        }

        dest.put(face, quads);
    }

    @Deprecated(forRemoval = true, since = "26.1")
    private void bakeLegacy4x4(ModelBaker baker, ModelState state, Direction face, Direction cull, CuboidFace.UVs uvs, Map<Direction, BakedQuad[]> dest, Set<Direction> unculled, Material.Baked material, Vector3f[] offsets) {
        var quads = new BakedQuad[CTMLogic4x4.values().length];

        for (var logic : CTMLogic4x4.values()) {
            var faceDef = new CuboidFace(cull, tintIndex, "", logic.remapUVs(uvs), Quadrant.R0);
            if (faceDef.cullForDirection() == null) unculled.add(face);

            quads[logic.ordinal()] = FaceBakery.bakeQuad(baker, offsets[0], offsets[1], faceDef, material, face, state, null, shade, emissivity);
        }

        dest.put(face, quads);
    }
}
