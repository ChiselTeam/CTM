package io.github.chiselteam.ctm.client.unbaked;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.chiselteam.ctm.client.baked.LayeredCTMBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record UnbakedLayeredCTMBlockStateModel(List<UnbakedConnectedTextureBlockStateModel> layers) implements CustomUnbakedBlockStateModel {

    public static final MapCodec<UnbakedLayeredCTMBlockStateModel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            UnbakedConnectedTextureBlockStateModel.CODEC.codec().listOf()
                    .fieldOf("layers")
                    .forGetter(UnbakedLayeredCTMBlockStateModel::layers)
    ).apply(instance, UnbakedLayeredCTMBlockStateModel::new));

    public UnbakedLayeredCTMBlockStateModel(List<UnbakedConnectedTextureBlockStateModel> layers) {
        if (layers.isEmpty()) throw new IllegalArgumentException("A layered CTM model requires at least one layer");
        this.layers = List.copyOf(layers);
    }

    @Override
    public void resolveDependencies(@NonNull Resolver resolver) {
        layers.forEach(layer -> layer.resolveDependencies(resolver));
    }

    @Override
    public @NonNull BlockStateModel bake(@NonNull ModelBaker baker) {
        return new LayeredCTMBlockStateModel(layers.stream().map(layer -> layer.bake(baker)).toList());
    }

    @Override
    public @NonNull MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return CODEC;
    }
}
