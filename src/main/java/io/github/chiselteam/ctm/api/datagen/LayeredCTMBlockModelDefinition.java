package io.github.chiselteam.ctm.api.datagen;
import com.mojang.serialization.MapCodec;
import io.github.chiselteam.ctm.client.unbaked.UnbakedLayeredCTMBlockStateModel;
/** Model data emitted as a geometry-loader model in 1.21.1. */
public record LayeredCTMBlockModelDefinition(UnbakedLayeredCTMBlockStateModel model) {
    public static final MapCodec<LayeredCTMBlockModelDefinition> CODEC = UnbakedLayeredCTMBlockStateModel.CODEC.xmap(LayeredCTMBlockModelDefinition::new, LayeredCTMBlockModelDefinition::model);
}
