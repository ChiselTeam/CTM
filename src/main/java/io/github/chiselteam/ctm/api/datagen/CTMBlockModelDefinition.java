package io.github.chiselteam.ctm.api.datagen;
import com.mojang.serialization.MapCodec;
import io.github.chiselteam.ctm.client.unbaked.UnbakedConnectedTextureBlockStateModel;
/** Model data emitted as a geometry-loader model in 1.21.1. */
public record CTMBlockModelDefinition(UnbakedConnectedTextureBlockStateModel model) {
    public static final MapCodec<CTMBlockModelDefinition> CODEC = UnbakedConnectedTextureBlockStateModel.CODEC.xmap(CTMBlockModelDefinition::new, CTMBlockModelDefinition::model);
}
