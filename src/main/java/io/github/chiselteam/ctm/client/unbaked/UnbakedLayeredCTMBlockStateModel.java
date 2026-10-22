package io.github.chiselteam.ctm.client.unbaked;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.chiselteam.ctm.client.baked.LayeredCTMBlockStateModel;
import io.github.chiselteam.ctm.client.model.CTMBakedModel;
import io.github.chiselteam.ctm.client.model.CTMBakingContext;
import io.github.chiselteam.ctm.client.model.CTMUnbakedModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record UnbakedLayeredCTMBlockStateModel(List<UnbakedConnectedTextureBlockStateModel> layers) implements CTMUnbakedModel {

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
    public void resolveDependencies(@NotNull Resolver resolver) {
        layers.forEach(layer -> layer.resolveDependencies(resolver));
    }

    @Override
    public @NotNull CTMBakedModel bake(@NotNull CTMBakingContext baker) {
        return new LayeredCTMBlockStateModel(layers.stream().map(layer -> layer.bake(baker)).toList());
    }

    @Override
    public @NotNull MapCodec<? extends CTMUnbakedModel> codec() {
        return CODEC;
    }
}
