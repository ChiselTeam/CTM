package io.github.chiselteam.ctm.api.datagen;

import io.github.chiselteam.ctm.client.unbaked.UnbakedConnectedTextureBlockStateModel;

@FunctionalInterface
public interface UnbakedMutator {
    UnbakedConnectedTextureBlockStateModel apply(UnbakedConnectedTextureBlockStateModel model);
}
