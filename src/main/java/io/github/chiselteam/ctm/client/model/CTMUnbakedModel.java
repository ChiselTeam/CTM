package io.github.chiselteam.ctm.client.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.chiselteam.ctm.client.unbaked.UnbakedConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.client.unbaked.UnbakedEldritchBlockStateModel;
import io.github.chiselteam.ctm.client.unbaked.UnbakedLayeredCTMBlockStateModel;
import net.minecraft.resources.ResourceLocation;

public interface CTMUnbakedModel {
    Codec<CTMUnbakedModel> CODEC = Codec.lazyInitialized(() ->
            Codec.STRING.dispatch("type", CTMUnbakedModel::type, type -> switch (type) {
                case "ctm:connected_texture_model" -> UnbakedConnectedTextureBlockStateModel.CODEC;
                case "ctm:layered_connected_texture_model" -> UnbakedLayeredCTMBlockStateModel.CODEC;
                case "ctm:eldritch_model" -> UnbakedEldritchBlockStateModel.CODEC;
                case "minecraft:model" -> CTMReferencedModel.CODEC;
                default -> throw new IllegalArgumentException("Unknown CTM model type: " + type);
            }));

    private String type() {
        if (this instanceof UnbakedConnectedTextureBlockStateModel) return "ctm:connected_texture_model";
        if (this instanceof UnbakedLayeredCTMBlockStateModel) return "ctm:layered_connected_texture_model";
        if (this instanceof UnbakedEldritchBlockStateModel) return "ctm:eldritch_model";
        return "minecraft:model";
    }

    CTMBakedModel bake(CTMBakingContext baker);

    MapCodec<? extends CTMUnbakedModel> codec();

    void resolveDependencies(Resolver resolver);

    interface Resolver {
        void markDependency(ResourceLocation location);
    }
}
