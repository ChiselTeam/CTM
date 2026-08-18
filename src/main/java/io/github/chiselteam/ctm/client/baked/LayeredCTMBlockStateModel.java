package io.github.chiselteam.ctm.client.baked;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Composes multiple CTM models in draw order. Each child computes its own
 * geometry key, so layers may use different CTM kinds and connection rules.
 */
public final class LayeredCTMBlockStateModel implements DynamicBlockStateModel {

    private final List<BlockStateModel> layers;
    private final Material.Baked particleMaterial;
    private final int materialFlags;

    public LayeredCTMBlockStateModel(List<BlockStateModel> layers) {
        if (layers.isEmpty()) throw new IllegalArgumentException("A layered CTM model requires at least one layer");
        this.layers = List.copyOf(layers);
        this.particleMaterial = layers.getFirst().particleMaterial();
        this.materialFlags = layers.stream()
                .mapToInt(BlockStateModel::materialFlags)
                .reduce(0, (left, right) -> left | right);
    }

    @Override
    public @NonNull Object createGeometryKey(@NonNull BlockAndTintGetter level, @NonNull BlockPos pos, @NonNull BlockState state, @NonNull RandomSource random) {
        return new GeometryKey(layers.stream()
                .map(layer -> layer.createGeometryKey(level, pos, state, random))
                .toList());
    }

    @Override
    public void collectParts(@NonNull BlockAndTintGetter level, @NonNull BlockPos pos, @NonNull BlockState state, @NonNull RandomSource random, @NonNull List<BlockStateModelPart> parts) {
        layers.forEach(layer -> layer.collectParts(level, pos, state, random, parts));
    }

    @Override
    public Material.@NonNull Baked particleMaterial() {
        return particleMaterial;
    }

    @Override
    public int materialFlags() {
        return materialFlags;
    }

    private record GeometryKey(List<Object> layerKeys) {}
}
