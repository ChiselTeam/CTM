package io.github.chiselteam.ctm.client.unbaked;

import io.github.chiselteam.ctm.client.baked.EldritchBlockStateModel;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.chiselteam.ctm.impl.model.CTMBakeCache;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.jspecify.annotations.NonNull;

import net.minecraft.client.resources.model.UnbakedModel;

public class UnbakedEldritchBlockStateModel implements CustomUnbakedBlockStateModel, UnbakedModel {

    private final BlockStateModel.Unbaked inner;

    public UnbakedEldritchBlockStateModel(BlockStateModel.Unbaked inner) {
        this.inner = inner;
    }

    public static final MapCodec<UnbakedEldritchBlockStateModel> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    BlockStateModel.Unbaked.CODEC.fieldOf("model").forGetter(m -> m.inner)
            ).apply(instance, UnbakedEldritchBlockStateModel::new)
    );

    @Override
    public void resolveDependencies(ResolvableModel.@NonNull Resolver resolver) {
        inner.resolveDependencies(resolver);
    }

    @Override
    public BlockStateModel.@NonNull UnbakedRoot asRoot() {
        return CTMBakeCache.root(this);
    }

    private final CTMBakeCache.Memo bakeMemo = new CTMBakeCache.Memo();

    @Override
    public @NonNull BlockStateModel bake(@NonNull ModelBaker baker) {
        return bakeMemo.bake(baker, () -> bakeUncached(baker));
    }

    private BlockStateModel bakeUncached(ModelBaker baker) {
        return new EldritchBlockStateModel(inner.bake(baker));
    }

    @Override
    public @NonNull MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return CODEC;
    }

    public BlockStateModel.Unbaked inner() {
        return inner;
    }
}
