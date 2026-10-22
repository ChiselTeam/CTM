package io.github.chiselteam.ctm.client.unbaked;

import com.mojang.datafixers.util.Pair;
import io.github.chiselteam.ctm.client.model.CTMFace.Quadrant;
import com.mojang.serialization.MapCodec;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.*;
import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;
import io.github.chiselteam.ctm.client.AbstractUnbakedConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.client.baked.MultiblockCTMBlockStateModel;
import io.github.chiselteam.ctm.impl.texture.CTMTextureSet;
import io.github.chiselteam.ctm.client.model.CTMBakedModel;
import io.github.chiselteam.ctm.client.model.CTMModelState;
import io.github.chiselteam.ctm.client.model.CTMModelVariant;
import io.github.chiselteam.ctm.client.model.CTMBakingContext;
import net.minecraft.client.renderer.block.model.BlockModel;
import io.github.chiselteam.ctm.client.model.CTMFace;
import io.github.chiselteam.ctm.client.model.CTMFaceBakery;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import io.github.chiselteam.ctm.client.model.CTMUnbakedModel;
import org.joml.Vector3f;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MultiblockUnbakedCTMModel extends AbstractUnbakedConnectedTextureBlockStateModel {

    public MultiblockUnbakedCTMModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, ResourceLocation> textureSlots, CTMModelVariant.SimpleModelState modelState) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots, modelState);
    }

    public MultiblockUnbakedCTMModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, ResourceLocation> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots);
    }

    public MultiblockUnbakedCTMModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, ResourceLocation> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, eldritch, connectionPredicate, overlays, textureSlots);
    }

    public MultiblockUnbakedCTMModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity) {
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

        var baseMaterial = getMaterial("base_texture");
        if (baseMaterial == null) baseMaterial = getMaterial("layer0");

        var bakedBase = bakeMaterial(baker, baseMaterial);
        var bakedParticle = bakeMaterial(baker, getMaterial("particle"));
        if (bakedParticle == null) bakedParticle = bakedBase;

        var textures2x2 = bake2x2Textures(baker, model);
        var textures3x3 = bake3x3Textures(baker, model);
        var textures4x4 = bake4x4Textures(baker, model);

        switch (variant.kind().multiblockSize()) {
            case 2 -> textures2x2.requireComplete(modelLocation.toString());
            case 3 -> textures3x3.requireComplete(modelLocation.toString());
            case 4 -> textures4x4.requireComplete(modelLocation.toString());
            default -> throw new IllegalArgumentException("Unsupported multiblock kind: " + variant.kind());
        }
        if (bakedParticle == null) bakedParticle = switch (variant.kind().multiblockSize()) {
            case 2 -> textures2x2.get(CTMLogic2x2.TOP_LEFT);
            case 3 -> textures3x3.get(CTMLogic3x3.TOP_LEFT);
            default -> textures4x4.get(CTMLogic4x4.ROW0_COL0);
        };

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
                    var baseFace = new CTMFace(cull, baseTintIndex, "", faceUVs, Quadrant.R0);
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

                    baseQuadsList.add(CTMFaceBakery.bakeQuad(baker, qFrom, qTo, baseFace, bakedBase, face, state, null, shade, baseEmissivity));
                } else {
                    for (int cornerIndex = 0; cornerIndex < 4; cornerIndex++) {
                        var corner = face.getNormal()
                                .offset(planeDirections[cornerIndex].getNormal())
                                .offset(planeDirections[(cornerIndex + 1) % 4].getNormal())
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

                        CTMFace.UVs qUvs = getRelativeUVs(face, qFrom, qTo);
                        CTMFace baseFace = new CTMFace(cull, baseTintIndex, "", qUvs, Quadrant.R0);

                        baseQuadsList.add(CTMFaceBakery.bakeQuad(baker, qFrom, qTo, baseFace, bakedBase, face, state, null, shade, baseEmissivity));
                    }
                }
            }

            if (!baseQuadsList.isEmpty()) baseQuads.put(face, baseQuadsList.toArray(new BakedQuad[0]));
            var faceUvs = getRelativeUVs(face, from, to);
            var offsets = getOffsets(face, from, to);

            if (textures2x2.isComplete())
                bakeStandaloneMultiblock(baker, state, face, cull, faceUvs, mb2x2Quads, unculledFaces, textures2x2, offsets);

            if (textures3x3.isComplete())
                bakeStandaloneMultiblock(baker, state, face, cull, faceUvs, mb3x3Quads, unculledFaces, textures3x3, offsets);

            if (textures4x4.isComplete())
                bakeStandaloneMultiblock(baker, state, face, cull, faceUvs, mb4x4Quads, unculledFaces, textures4x4, offsets);
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

    private CTMTextureSet<CTMLogic2x2> bake2x2Textures(CTMBakingContext baker, BlockModel model) {
        var textures = new CTMTextureSet<>(CTMLogic2x2.class);

        textures.put(CTMLogic2x2.TOP_LEFT, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_2X2_TOP_LEFT)));
        textures.put(CTMLogic2x2.TOP_RIGHT, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_2X2_TOP_RIGHT)));
        textures.put(CTMLogic2x2.BOTTOM_LEFT, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_2X2_BOTTOM_LEFT)));
        textures.put(CTMLogic2x2.BOTTOM_RIGHT, bakeMaterial(baker, getMaterial(CTMTextureKeys.MULTIBLOCK_2X2_BOTTOM_RIGHT)));

        return textures;
    }

    private CTMTextureSet<CTMLogic3x3> bake3x3Textures(CTMBakingContext baker, BlockModel model) {
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

    private CTMTextureSet<CTMLogic4x4> bake4x4Textures(CTMBakingContext baker, BlockModel model) {
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

    private <T extends Enum<T>> void bakeStandaloneMultiblock(CTMBakingContext baker, CTMModelState state, Direction face, Direction cull, CTMFace.UVs uvs, Map<Direction, BakedQuad[]> dest, Set<Direction> unculled, CTMTextureSet<T> textures, Vector3f[] offsets) {
        var values = textures.getType().getEnumConstants();
        var quads = new BakedQuad[values.length];

        for (T logic : values) {
            var material = textures.get(logic);
            if (material == null) continue;

            var faceDef = new CTMFace(cull, tintIndex, "", uvs, Quadrant.R0);
            if (faceDef.cullForDirection() == null) unculled.add(face);

            quads[logic.ordinal()] = CTMFaceBakery.bakeQuad(baker, offsets[0], offsets[1], faceDef, material, face, state, null, shade, emissivity);
        }

        dest.put(face, quads);
    }

}
