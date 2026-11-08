package io.github.chiselteam.ctm.impl.model;

import io.github.chiselteam.ctm.client.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Keeps only captured textures and geometry inputs until first use. */
public final class DeferredCTMModel implements CTMBakedModel, CTMQuadStorage {
    private final String family;
    private final BakedTexture particle;
    private final boolean ambientOcclusion;
    private volatile CTMBakedModel realized;
    private Supplier<CTMBakedModel> recipe;
    private CTMBakeCache.Scope scope;

    public DeferredCTMModel(String family, BakedTexture particle, boolean ambientOcclusion,
                            Supplier<CTMBakedModel> recipe, CTMBakeCache.Scope scope) {
        this.family = family;
        this.particle = particle;
        this.ambientOcclusion = ambientOcclusion;
        this.recipe = Objects.requireNonNull(recipe);
        this.scope = Objects.requireNonNull(scope);
    }

    public record Inputs(java.util.Map<String, net.minecraft.client.resources.model.Material> materials,
                         java.util.Map<net.minecraft.client.resources.model.Material, net.minecraft.client.renderer.texture.TextureAtlasSprite> sprites,
                         CTMModelState state) implements java.util.function.Function<net.minecraft.client.resources.model.Material, net.minecraft.client.renderer.texture.TextureAtlasSprite> {
        public Inputs {
            materials = java.util.Map.copyOf(materials);
            sprites = java.util.Map.copyOf(sprites);
        }

        @Override
        public net.minecraft.client.renderer.texture.TextureAtlasSprite apply(net.minecraft.client.resources.model.Material material) {
            return Objects.requireNonNull(sprites.get(material), "Uncaptured material " + material);
        }
    }

    private CTMBakedModel model() {
        var result = realized;
        if (result == null) synchronized (this) {
            result = realized;
            if (result == null) {
                result = Objects.requireNonNull(recipe.get());
                if (!Objects.equals(result.particleMaterial(), particle)
                        || result.useAmbientOcclusion() != ambientOcclusion)
                    throw new IllegalStateException("Deferred metadata mismatch: " + family);
                scope.finish(result);
                realized = result;
                recipe = null;
                scope = null;
            }
        }
        return result;
    }

    @Override public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return model().createGeometryKey(level, pos, state, random);
    }
    @Override public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<CTMModelPart> output) {
        model().collectParts(level, pos, state, random, output);
    }
    @Override public void collectParts(RandomSource random, List<CTMModelPart> output) {
        model().collectParts(random, output);
    }
    @Override public BakedTexture particleMaterial() { return particle; }
    @Override public boolean useAmbientOcclusion() { return ambientOcclusion; }
    @Override public void visitChildModels(Consumer<CTMBakedModel> visitor) {
        var model = realized;
        if (model != null) visitor.accept(model);
    }
}
