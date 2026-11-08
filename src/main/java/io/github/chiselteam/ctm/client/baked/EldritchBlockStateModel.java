package io.github.chiselteam.ctm.client.baked;

import io.github.chiselteam.ctm.client.AbstractConnectedTextureBlockStateModel;
import io.github.chiselteam.ctm.impl.model.DeferredCTMModel;
import io.github.chiselteam.ctm.impl.model.EldritchQuadTransformer;
import io.github.chiselteam.ctm.impl.model.CTMQuadStorage;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EldritchBlockStateModel implements DynamicBlockStateModel, CTMQuadStorage {

    private static final int MAX_CACHE_ENTRIES = 4096;

    private final BlockStateModel delegate;
    private final boolean cacheByGeometryKey;

    private final Map<Long, CachedParts> cache = Collections.synchronizedMap(
            new LinkedHashMap<>(64, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Long, CachedParts> eldest) {
                    return size() > MAX_CACHE_ENTRIES;
                }
            }
    );

    public EldritchBlockStateModel(BlockStateModel delegate) {
        this.delegate = delegate;
        this.cacheByGeometryKey = delegate.getClass() == SingleVariant.class
                || delegate.getClass() == DeferredCTMModel.class
                || (delegate instanceof AbstractConnectedTextureBlockStateModel<?>
                && delegate.getClass().getPackageName().equals(EldritchBlockStateModel.class.getPackageName()));
    }

    @Override
    public void visitChildModels(Consumer<BlockStateModel> visitor) {
        visitor.accept(delegate);
    }

    @Override
    public @Nullable Object createGeometryKey(@NonNull BlockAndTintGetter level, @NonNull BlockPos pos, @NonNull BlockState state, @NonNull RandomSource random) {
        var delegateKey = computeDelegateGeometryKey(level, pos, state, random);
        return delegateKey == null ? null : new EldritchGeometryKey(pos.asLong(), delegateKey);
    }

    private Object computeDelegateGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return delegate.createGeometryKey(level, pos, state, random);
    }

    @Override
    public void collectParts(@NonNull BlockAndTintGetter level, @NonNull BlockPos pos, @NonNull BlockState state, @NonNull RandomSource random, @NonNull List<BlockStateModelPart> parts) {
        var delegateKey = cacheByGeometryKey ? computeDelegateGeometryKey(level, pos, state, random) : null;
        long position = pos.asLong();
        var cached = delegateKey == null ? null : cache.get(position);

        if (cached != null && cached.delegateKey().equals(delegateKey)) {
            parts.addAll(cached.parts());
            return;
        }
        List<BlockStateModelPart> delegateParts = new ArrayList<>();
        delegate.collectParts(level, pos, state, random, delegateParts);
        List<BlockStateModelPart> transformed = new ArrayList<>(delegateParts.size());
        for (BlockStateModelPart part : delegateParts) {
            transformed.add(transformPart(part, pos));
        }

        List<BlockStateModelPart> immutable = List.copyOf(transformed);
        if (delegateKey != null) {
            synchronized (cache) {
                if (cache.get(position) == cached) cache.put(position, new CachedParts(delegateKey, immutable));
            }
        }
        parts.addAll(immutable);
    }

    @Override
    public void collectParts(@NonNull RandomSource random, @NonNull List<BlockStateModelPart> parts) {
        List<BlockStateModelPart> delegateParts = new ArrayList<>();
        delegate.collectParts(random, delegateParts);
        for (BlockStateModelPart part : delegateParts) {
            parts.add(transformPart(part, BlockPos.ZERO));
        }
    }

    @Override
    public Material.@NonNull Baked particleMaterial() {
        return delegate.particleMaterial();
    }

    @Override
    public int materialFlags() {
        return delegate.materialFlags();
    }

    private static BlockStateModelPart transformPart(BlockStateModelPart part, BlockPos pos) {
        Map<Direction, List<BakedQuad>> newSided = new EnumMap<>(Direction.class);
        for (Direction face : Direction.values()) {
            List<BakedQuad> faceQuads = part.getQuads(face);
            if (!faceQuads.isEmpty()) {
                newSided.put(face, List.copyOf(EldritchQuadTransformer.transform(faceQuads, pos)));
            } else {
                newSided.put(face, List.of());
            }
        }
        List<BakedQuad> unculled = part.getQuads(null);
        List<BakedQuad> newUnculled = unculled.isEmpty()
                ? List.of()
                : List.copyOf(EldritchQuadTransformer.transform(unculled, pos));
        return new TransformedPart(newSided, newUnculled, part);
    }

    private record TransformedPart(Map<Direction, List<BakedQuad>> sidedQuads, List<BakedQuad> unculledQuads, BlockStateModelPart source) implements BlockStateModelPart {
        @Override
        public @NonNull List<BakedQuad> getQuads(@Nullable Direction side) {
            return side == null ? unculledQuads : sidedQuads.get(side);
        }

        @Override
        public int materialFlags() {
            return source.materialFlags();
        }

        @Override
        public Material.@NonNull Baked particleMaterial() {
            return source.particleMaterial();
        }

        @Override
        public boolean useAmbientOcclusion() {
            return source.useAmbientOcclusion();
        }
    }

    private record CachedParts(Object delegateKey, List<BlockStateModelPart> parts) {}
    private record EldritchGeometryKey(long pos, Object delegateKey) {}
}
