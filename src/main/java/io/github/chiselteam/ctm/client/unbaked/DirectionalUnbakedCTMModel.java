package io.github.chiselteam.ctm.client.unbaked;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quadrant;
import com.mojang.serialization.MapCodec;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.api.strategy.CTMLogic;
import io.github.chiselteam.ctm.api.strategy.CTMLogicHorizontal;
import io.github.chiselteam.ctm.api.strategy.CTMLogicVertical;
import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;
import io.github.chiselteam.ctm.client.AbstractUnbakedConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.client.baked.DirectionalCTMBlockStateModel;
import io.github.chiselteam.ctm.impl.texture.CTMTextureSet;
import io.github.chiselteam.ctm.util.ModelRecipe;
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
import org.jspecify.annotations.Nullable;

import java.util.*;

public class DirectionalUnbakedCTMModel extends AbstractUnbakedConnectedTextureBlockStateModel {

    private Material.Baked bakedBase, bakedTop, bakedBottom;

    public DirectionalUnbakedCTMModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, @Nullable Direction shadeDirectionOverride, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots, Variant.SimpleModelState modelState) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shadeDirectionOverride, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots, modelState);
    }

    public DirectionalUnbakedCTMModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, @Nullable Direction shadeDirectionOverride, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shadeDirectionOverride, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots);
    }

    public DirectionalUnbakedCTMModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, @Nullable Direction shadeDirectionOverride, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots) {
        super(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shadeDirectionOverride, eldritch, connectionPredicate, overlays, textureSlots);
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
        prepareBakery(baker);

        bakedBase = bakeMaterial(baker, getMaterial(CTMTextureKeys.BASE));
        if (bakedBase == null) bakedBase = bakeMaterial(baker, getMaterial("layer0"));
        bakedTop = bakeMaterial(baker, getMaterial("top"));
        bakedBottom = bakeMaterial(baker, getMaterial("bottom"));
        var bakedParticle = bakeMaterial(baker, getMaterial(CTMTextureKeys.PARTICLE));

        var horizontalTextures = bakeHorizontalTextures(baker, model);
        var verticalTextures = bakeVerticalTextures(baker, model);

        var usesStandaloneHorizontal = horizontalTextures.isComplete();
        var usesStandaloneVertical = verticalTextures.isComplete();

        if (bakedParticle == null) {
            bakedParticle = bakedBase != null ? bakedBase : variant.kind().isCTMV()
                    ? verticalTextures.get(CTMLogicVertical.NONE)
                    : horizontalTextures.get(CTMLogicHorizontal.NONE);
        }

        Map<Direction, BakedQuad[]> baseQuads = new EnumMap<>(Direction.class);
        Map<Direction, BakedQuad[]> horizontalQuads = new EnumMap<>(Direction.class);
        Map<Direction, BakedQuad[]> verticalQuads = new EnumMap<>(Direction.class);
        Set<Direction> unculledFaces = new HashSet<>();

        var horizontalRecipe = new ModelRecipe.Builder().baker(baker).state(state).bakedQuads(horizontalQuads).unculledFaces(unculledFaces);
        var verticalRecipe = new ModelRecipe.Builder().baker(baker).state(state).bakedQuads(verticalQuads).unculledFaces(unculledFaces);

        Vector3f from = element.getFirst();
        Vector3f to = element.getSecond();

        for (var face : Direction.values()) {
            var baseForFace = baseTextureForFace(face);
            var baseQuadsList = new ArrayList<BakedQuad>();
            var faceUVs = getRelativeUVs(face, from, to);
            var offsets = getOffsets(face, from, to);

            for (int cornerIndex = 0; cornerIndex < 4; cornerIndex++) {
                if (baseForFace != null) {
                    baseQuadsList.add(bakeBaseQuadForFace(baker, baseForFace, state, face, cornerIndex, from, to));
                }
            }

            if (!baseQuadsList.isEmpty()) baseQuads.put(face, baseQuadsList.toArray(new BakedQuad[0]));

            if ((variant.kind().isBookshelfLike() || variant.kind().isCTMH()) && face.getAxis().isHorizontal()) {
                var recipe = horizontalRecipe
                        .face(face)
                        .cull(getCullface(face, from, to))
                        .faceUvs(faceUVs)
                        .offsets(offsets)
                        .base(baseForFace)
                        .build();

                if (usesStandaloneHorizontal) {
                    bakeStandaloneHorizontalQuads(recipe, horizontalTextures);
                }
            }

            if (variant.kind().isCTMV() && face.getAxis().isHorizontal()) {
                var recipe = verticalRecipe
                        .face(face)
                        .cull(getCullface(face, from, to))
                        .faceUvs(faceUVs)
                        .offsets(offsets)
                        .base(baseForFace)
                        .build();
                if (usesStandaloneVertical) {
                    bakeStandaloneVerticalQuads(recipe, verticalTextures);
                }
            }
        }

        var bakedOverlays = bakeOverlays();
        var directionSource = new EnumMap<>(Direction.class);
        for (var entry : verticalQuads.entrySet()) {
            directionSource.put(entry.getKey(), entry.getValue()[0]);
        }
        for (var entry : baseQuads.entrySet()) directionSource.putIfAbsent(entry.getKey(), entry.getValue()[0]);
        var bakedConnectedFaces = remapDirections(connectedFaces, directionSource);
        var bakedUnculledFaces = remapDirections(unculledFaces, directionSource);
        var ruleQuads = bakeOverlayQuads(baker, bakedOverlays, from, to, state);
        ruleQuads.replaceAll((_, quads) -> remapQuadDirections(quads));

        return new DirectionalCTMBlockStateModel(bakedConnectedFaces, bakedUnculledFaces, renderOverlayOnAllFaces, remapQuadDirections(baseQuads), remapQuadDirections(horizontalQuads), remapQuadDirections(verticalQuads), bakedParticle != null ? bakedParticle.sprite() : null, variant, connectionPredicate, bakedOverlays, ruleQuads, ambientOcclusion);
    }

    private Material.Baked baseTextureForFace(Direction face) {
        if (variant.kind().isCTMH() || variant.kind().isCTMV()) {
            if (face == Direction.UP && bakedTop != null) return bakedTop;
            else if (face == Direction.DOWN && bakedBottom != null) return bakedBottom;
        }
        return bakedBase;
    }

    private BakedQuad bakeBaseQuadForFace(ModelBaker baker, Material.Baked baseForFace, ModelState state, Direction face, int cornerIndex, Vector3f from, Vector3f to) {
        var planeDirections = CTMLogic.AXIS_PLANE_DIRECTIONS[face.getAxis().ordinal()];
        int center = 8;

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

        var qUvs = getRelativeUVs(face, qFrom, qTo);
        var baseFace = new CuboidFace(getCullface(face, from, to), baseTintIndex, "", qUvs, Quadrant.R0);

        return FaceBakery.bakeQuad(baker, qFrom, qTo, baseFace, baseForFace, face, state, null, shadeDirectionOverride, baseEmissivity);
    }

    private CTMTextureSet<CTMLogicHorizontal> bakeHorizontalTextures(ModelBaker baker, ResolvedModel model) {
        var textures = new CTMTextureSet<>(CTMLogicHorizontal.class);
        textures.put(CTMLogicHorizontal.NONE, bakeMaterial(baker, getMaterial(CTMTextureKeys.HORIZONTAL_NONE)));
        textures.put(CTMLogicHorizontal.BOTH, bakeMaterial(baker, getMaterial(CTMTextureKeys.HORIZONTAL_BOTH)));
        textures.put(CTMLogicHorizontal.LEFT, bakeMaterial(baker, getMaterial(CTMTextureKeys.HORIZONTAL_LEFT)));
        textures.put(CTMLogicHorizontal.RIGHT, bakeMaterial(baker, getMaterial(CTMTextureKeys.HORIZONTAL_RIGHT)));
        return textures;
    }

    private CTMTextureSet<CTMLogicVertical> bakeVerticalTextures(ModelBaker baker, ResolvedModel model) {
        var textures = new CTMTextureSet<>(CTMLogicVertical.class);
        textures.put(CTMLogicVertical.NONE, bakeMaterial(baker, getMaterial(CTMTextureKeys.VERTICAL_NONE)));
        textures.put(CTMLogicVertical.BOTH, bakeMaterial(baker, getMaterial(CTMTextureKeys.VERTICAL_BOTH)));
        textures.put(CTMLogicVertical.TOP, bakeMaterial(baker, getMaterial(CTMTextureKeys.VERTICAL_TOP)));
        textures.put(CTMLogicVertical.BOTTOM, bakeMaterial(baker, getMaterial(CTMTextureKeys.VERTICAL_BOTTOM)));
        return textures;
    }

    private void bakeStandaloneHorizontalQuads(ModelRecipe recipe, CTMTextureSet<CTMLogicHorizontal> textures) {
        var quads = new BakedQuad[CTMLogicHorizontal.values().length];
        var from = recipe.offsets()[0];
        var to = recipe.offsets()[1];

        if (variant.kind().isBookshelfLike() && bakedBase != null) {
            float offset = 0.05F;
            from = new Vector3f(from).add(
                    recipe.face() == Direction.WEST ? -offset : 0,
                    recipe.face() == Direction.DOWN ? -offset : 0,
                    recipe.face() == Direction.NORTH ? -offset : 0
            );
            to = new Vector3f(to).add(
                    recipe.face() == Direction.EAST ? offset : 0,
                    recipe.face() == Direction.UP ? offset : 0,
                    recipe.face() == Direction.SOUTH ? offset : 0
            );
        }

        for (var logic : CTMLogicHorizontal.values()) {
            var material = textures.get(logic);
            if (material == null) continue;

            var connFace = new CuboidFace(recipe.cull(), tintIndex, "", recipe.faceUvs(), Quadrant.R0);
            if (connFace.cullForDirection() == null) recipe.unculledFaces().add(recipe.face());

            quads[logic.ordinal()] = FaceBakery.bakeQuad(recipe.baker(), from, to, connFace, material, recipe.face(), recipe.state(), null, shadeDirectionOverride, emissivity);
        }

        recipe.bakedQuads().put(recipe.face(), quads);
    }

    private void bakeStandaloneVerticalQuads(ModelRecipe recipe, CTMTextureSet<CTMLogicVertical> textures) {
        var quads = new BakedQuad[CTMLogicVertical.values().length];

        for (CTMLogicVertical logic : CTMLogicVertical.values()) {
            var material = textures.get(logic);
            if (material == null) continue;

            var connFace = new CuboidFace(recipe.cull(), tintIndex, "", recipe.faceUvs(), Quadrant.R0);
            if (connFace.cullForDirection() == null) recipe.unculledFaces().add(recipe.face());

            quads[logic.ordinal()] = FaceBakery.bakeQuad(recipe.baker(), recipe.offsets()[0], recipe.offsets()[1], connFace, material, recipe.face(), recipe.state(), null, shadeDirectionOverride, emissivity);
        }

        recipe.bakedQuads().put(recipe.face(), quads);
    }
}
