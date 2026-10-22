package io.github.chiselteam.ctm.client.model;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface CTMBakedModel extends BakedModel {
    ModelProperty<List<CTMModelPart>> PARTS = new ModelProperty<>();

    Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random);

    void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<CTMModelPart> parts);

    default void collectParts(RandomSource random, List<CTMModelPart> parts) {
    }

    BakedTexture particleMaterial();

    @Override
    default @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, ModelData data) {
        List<CTMModelPart> parts = new ArrayList<>();
        collectParts(level, pos, state, RandomSource.create(state.getSeed(pos)), parts);
        return data.derive().with(PARTS, List.copyOf(parts)).build();
    }

    @Override
    default @NotNull List<BakedQuad> getQuads(BlockState state, Direction side, @NotNull RandomSource random, ModelData data, RenderType renderType) {
        List<CTMModelPart> parts = data.get(PARTS);
        if (parts == null) {
            parts = new ArrayList<>();
            collectParts(random, parts);
        }
        List<BakedQuad> quads = new ArrayList<>();
        for (var part : parts) quads.addAll(part.getQuads(side));
        return quads;
    }

    @Override
    default @NotNull List<BakedQuad> getQuads(BlockState state, Direction side, @NotNull RandomSource random) {
        return getQuads(state, side, random, ModelData.EMPTY, null);
    }

    @Override
    default @NotNull TextureAtlasSprite getParticleIcon() {
        return particleMaterial().sprite();
    }

    @Override
    default boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    default boolean isGui3d() {
        return true;
    }

    @Override
    default boolean usesBlockLight() {
        return true;
    }

    @Override
    default boolean isCustomRenderer() {
        return false;
    }

    @Override
    default @NotNull ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    default @NotNull ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
