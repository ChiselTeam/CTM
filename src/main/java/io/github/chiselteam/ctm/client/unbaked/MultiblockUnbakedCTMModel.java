package io.github.chiselteam.ctm.client.unbaked;

import io.github.chiselteam.ctm.api.strategy.CTMLogic;
import io.github.chiselteam.ctm.api.strategy.CTMLogic4x4;
import io.github.chiselteam.ctm.api.strategy.CTMLogic2x2;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.CTMLogic3x3;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quadrant;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import io.github.chiselteam.ctm.client.AbstractUnbakedConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.client.baked.MultiblockCTMBlockStateModel;
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

public class MultiblockUnbakedCTMModel extends AbstractUnbakedConnectedTextureBlockStateModel {

    public MultiblockUnbakedCTMModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity) {
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

        TextureSlots textureSlots = model.getTopTextureSlots();
        Material baseMaterial = textureSlots.getMaterial("base_texture");
        if (baseMaterial == null) {
            baseMaterial = textureSlots.getMaterial("layer0");
        }
        Material particleMaterial = textureSlots.getMaterial("particle");

        Material.Baked bakedBase = (baseMaterial != null ? baker.materials().get(baseMaterial, model) : null);
        Material.Baked bakedParticle = particleMaterial != null ? baker.materials().get(particleMaterial, model) : bakedBase;

        Map<Direction, BakedQuad[]> baseQuads = new EnumMap<>(Direction.class);
        Map<Direction, BakedQuad[]> mb2x2Quads = new EnumMap<>(Direction.class);
        Map<Direction, BakedQuad[]> mb3x3Quads = new EnumMap<>(Direction.class);
        Map<Direction, BakedQuad[]> mb4x4Quads = new EnumMap<>(Direction.class);
        Set<Direction> unculledFaces = new HashSet<>();

        Vector3f from = element.getFirst();
        Vector3f to = element.getSecond();
        int center = 8;

        for (Direction face : Direction.values()) {
            Direction cull = getCullface(face, from, to);
            Direction[] planeDirections = CTMLogic.AXIS_PLANE_DIRECTIONS[face.getAxis().ordinal()];

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

                if (bakedBase != null) {
                    CuboidFace.UVs qUvs = getRelativeUVs(face, qFrom, qTo);
                    CuboidFace baseFace = new CuboidFace(cull, baseTintIndex, "", CTMLogic.NONE.remapUVs(qUvs), Quadrant.R0);
                    baseQuadList.add(FaceBakery.bakeQuad(baker, qFrom, qTo, baseFace, bakedBase, face, state, null, true, baseEmissivity));
                }
            }
            if (!baseQuadList.isEmpty()) {
                baseQuads.put(face, baseQuadList.toArray(new BakedQuad[0]));
            }

            CuboidFace.UVs faceUvs = getRelativeUVs(face, from, to);

            Vector3f[] offsets = getOffsets(face, from, to);
            bakeMultiblock(baker, textureSlots, model, "overlay_2x2", face, cull, state, faceUvs, mb2x2Quads, unculledFaces, CTMLogic2x2.values(), emissivity, tintIndex, offsets);
            bakeMultiblock(baker, textureSlots, model, "overlay_3x3", face, cull, state, faceUvs, mb3x3Quads, unculledFaces, CTMLogic3x3.values(), emissivity, tintIndex, offsets);
            bakeMultiblock(baker, textureSlots, model, "overlay_4x4", face, cull, state, faceUvs, mb4x4Quads, unculledFaces, CTMLogic4x4.values(), emissivity, tintIndex, offsets);
        }

        return new MultiblockCTMBlockStateModel(connectedFaces, unculledFaces, renderOverlayOnAllFaces, baseQuads, mb2x2Quads, mb3x3Quads, mb4x4Quads, bakedParticle != null ? bakedParticle.sprite() : null, variant);
    }

    private void bakeMultiblock(ModelBaker baker, TextureSlots slots, ResolvedModel model, String textureKey, Direction face, Direction cull, ModelState state, CuboidFace.UVs uvs, Map<Direction, BakedQuad[]> dest, Set<Direction> unculled, Enum<?>[] values, int emissivity, int tintIndex, Vector3f[] offsets) {
        Material material = slots.getMaterial(textureKey);
        if (material == null) return;

        Material.Baked baked = baker.materials().get(material, model);
        if (baked == null) return;

        BakedQuad[] quads = new BakedQuad[values.length];
        for (int i = 0; i < values.length; i++) {
            CuboidFace.UVs remapped;
            if (values[i] instanceof CTMLogic2x2 logic) remapped = logic.remapUVs(uvs);
            else if (values[i] instanceof CTMLogic3x3 logic) remapped = logic.remapUVs(uvs);
            else if (values[i] instanceof CTMLogic4x4 logic) remapped = logic.remapUVs(uvs);
            else remapped = uvs;

            CuboidFace faceDef = new CuboidFace(cull, tintIndex, "", remapped, Quadrant.R0);
            if (faceDef.cullForDirection() == null) unculled.add(face);
            quads[i] = FaceBakery.bakeQuad(baker, offsets[0], offsets[1], faceDef, baked, face, state, null, true, emissivity);
        }
        dest.put(face, quads);
    }
}
