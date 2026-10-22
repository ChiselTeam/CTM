package io.github.chiselteam.ctm.client.unbaked;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.chiselteam.ctm.client.baked.EldritchBlockStateModel;
import io.github.chiselteam.ctm.client.model.*;
public record UnbakedEldritchBlockStateModel(CTMUnbakedModel inner) implements CTMUnbakedModel {
    public static final MapCodec<UnbakedEldritchBlockStateModel> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.either(UnbakedConnectedTextureBlockStateModel.CODEC.codec(), CTMUnbakedModel.CODEC)
                    .<CTMUnbakedModel>xmap(either -> either.map(model -> model, model -> model), Either::right)
                    .fieldOf("model").forGetter(UnbakedEldritchBlockStateModel::inner)
    ).apply(i, UnbakedEldritchBlockStateModel::new));
    @Override public void resolveDependencies(Resolver resolver) { inner.resolveDependencies(resolver); }
    @Override public CTMBakedModel bake(CTMBakingContext baker) { return new EldritchBlockStateModel(inner.bake(baker)); }
    @Override public MapCodec<? extends CTMUnbakedModel> codec() { return CODEC; }
}
