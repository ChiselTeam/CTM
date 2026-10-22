package io.github.chiselteam.ctm.client.baked;

import io.github.chiselteam.ctm.client.model.BakedTexture;
import io.github.chiselteam.ctm.impl.model.CTMQuadStorage;
import net.minecraft.world.level.BlockAndTintGetter;
import io.github.chiselteam.ctm.client.model.CTMBakedModel;
import io.github.chiselteam.ctm.client.model.CTMModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public final class LayeredCTMBlockStateModel implements CTMBakedModel, CTMQuadStorage {

    private final List<CTMBakedModel> layers;
    private final BakedTexture particleMaterial;

    public LayeredCTMBlockStateModel(List<CTMBakedModel> layers) {
        if (layers.isEmpty()) throw new IllegalArgumentException("A layered CTM model requires at least one layer");
        this.layers = List.copyOf(layers);
        this.particleMaterial = layers.getFirst().particleMaterial();
    }

    @Override
    public void visitChildModels(Consumer<CTMBakedModel> visitor) {
        layers.forEach(visitor);
    }

    @Override
    public @NotNull Object createGeometryKey(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull RandomSource random) {
        return new GeometryKey(layers.stream()
                .map(layer -> layer.createGeometryKey(level, pos, state, random))
                .toList());
    }

    @Override
    public void collectParts(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull RandomSource random, @NotNull List<CTMModelPart> parts) {
        layers.forEach(layer -> layer.collectParts(level, pos, state, random, parts));
    }

    @Override
    public void collectParts(RandomSource random, List<CTMModelPart> parts) {
        layers.forEach(layer -> layer.collectParts(random, parts));
    }

    @Override
    public boolean useAmbientOcclusion() {
        return layers.getFirst().useAmbientOcclusion();
    }

    @Override
    public BakedTexture particleMaterial() {
        return particleMaterial;
    }

    private record GeometryKey(List<Object> layerKeys) {}
}
