package io.github.chiselteam.ctm.client.model;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record CTMReferencedModel(ResourceLocation model, CTMModelVariant.SimpleModelState modelState) implements CTMUnbakedModel {
    public static final MapCodec<CTMReferencedModel> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ResourceLocation.CODEC.fieldOf("model").forGetter(CTMReferencedModel::model),
            CTMModelVariant.SimpleModelState.MAP_CODEC.forGetter(CTMReferencedModel::modelState)
    ).apply(i, CTMReferencedModel::new));

    @Override
    public void resolveDependencies(Resolver resolver) {
        resolver.markDependency(model);
    }

    @Override
    public MapCodec<? extends CTMUnbakedModel> codec() {
        return CODEC;
    }

    @Override
    public CTMBakedModel bake(CTMBakingContext baker) {
        var rotation = baker.modelState().getRotation().compose(modelState.asModelState().getRotation());
        var state = new net.neoforged.neoforge.client.model.SimpleModelState(rotation, baker.modelState().isUvLocked() || modelState.uvLock());
        var delegate = Objects.requireNonNull(baker.baker().bake(model, state, baker.spriteGetter()), "Missing referenced model: " + model);
        return new CTMBakedModel() {
            @Override
            public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
                return new Object();
            }

            @Override
            public BakedTexture particleMaterial() {
                return new BakedTexture(delegate.getParticleIcon(), false);
            }

            @Override
            public boolean useAmbientOcclusion() {
                return delegate.useAmbientOcclusion();
            }

            @Override
            public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<CTMModelPart> parts) {
                append(state, delegate.getModelData(level, pos, state, ModelData.EMPTY), random, parts);
            }

            @Override
            public void collectParts(RandomSource random, List<CTMModelPart> parts) {
                append(null, ModelData.EMPTY, random, parts);
            }

            private void append(BlockState state, ModelData data, RandomSource random, List<CTMModelPart> parts) {
                Map<Direction, List<BakedQuad>> sided = new EnumMap<>(Direction.class);
                for (Direction face : Direction.values()) sided.put(face, List.copyOf(delegate.getQuads(state, face, random, data, null)));
                var unculled = List.copyOf(delegate.getQuads(state, null, random, data, null));
                parts.add(new io.github.chiselteam.ctm.api.model.ConnectedTextureBlockModelPart(sided, unculled, particleMaterial(), useAmbientOcclusion()));
            }
        };
    }
}
