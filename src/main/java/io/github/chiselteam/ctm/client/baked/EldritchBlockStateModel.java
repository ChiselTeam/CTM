package io.github.chiselteam.ctm.client.baked;

import io.github.chiselteam.ctm.client.model.BakedTexture;
import io.github.chiselteam.ctm.impl.model.EldritchQuadTransformer;
import io.github.chiselteam.ctm.impl.model.CTMQuadStorage;
import net.minecraft.world.level.BlockAndTintGetter;
import io.github.chiselteam.ctm.client.model.CTMBakedModel;
import io.github.chiselteam.ctm.client.model.CTMModelPart;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EldritchBlockStateModel implements CTMBakedModel, CTMQuadStorage {

    private static final int MAX_CACHE_ENTRIES = 4096;

    private final CTMBakedModel delegate;

    private final Map<Object, List<CTMModelPart>> cache = Collections.synchronizedMap(
            new LinkedHashMap<>(64, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Object, List<CTMModelPart>> eldest) {
                    return size() > MAX_CACHE_ENTRIES;
                }
            }
    );

    public EldritchBlockStateModel(CTMBakedModel delegate) {
        this.delegate = delegate;
    }

    @Override
    public void visitChildModels(Consumer<CTMBakedModel> visitor) {
        visitor.accept(delegate);
    }

    @Override
    public @NotNull Object createGeometryKey(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull RandomSource random) {
        Object delegateKey = computeDelegateGeometryKey(level, pos, state, random);
        return new EldritchGeometryKey(pos.asLong(), delegateKey);
    }

    private Object computeDelegateGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return delegate.createGeometryKey(level, pos, state, random);
    }

    @Override
    public void collectParts(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull RandomSource random, @NotNull List<CTMModelPart> parts) {
        Object key = createGeometryKey(level, pos, state, random);
        List<CTMModelPart> cached = cache.get(key);
        if (cached != null) {
            parts.addAll(cached);
            return;
        }

        List<CTMModelPart> delegateParts = new ArrayList<>();
        delegate.collectParts(level, pos, state, random, delegateParts);

        List<CTMModelPart> transformed = new ArrayList<>(delegateParts.size());
        for (CTMModelPart part : delegateParts) {
            transformed.add(transformPart(part, pos));
        }

        List<CTMModelPart> immutable = List.copyOf(transformed);
        cache.put(key, immutable);
        parts.addAll(immutable);
    }

    @Override
    public void collectParts(@NotNull RandomSource random, @NotNull List<CTMModelPart> parts) {
        List<CTMModelPart> delegateParts = new ArrayList<>();
        delegate.collectParts(random, delegateParts);
        for (CTMModelPart part : delegateParts) {
            parts.add(transformPart(part, BlockPos.ZERO));
        }
    }

    @Override
    public BakedTexture particleMaterial() {
        return delegate.particleMaterial();
    }

    @Override
    public boolean useAmbientOcclusion() { return delegate.useAmbientOcclusion(); }

    private static CTMModelPart transformPart(CTMModelPart part, BlockPos pos) {
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

    private record TransformedPart(
            Map<Direction, List<BakedQuad>> sidedQuads,
            List<BakedQuad> unculledQuads,
            CTMModelPart source
    ) implements CTMModelPart {

        @Override
        public @NotNull List<BakedQuad> getQuads(@Nullable Direction side) {
            return side == null ? unculledQuads : sidedQuads.get(side);
        }

        @Override
        public BakedTexture particleMaterial() {
            return source.particleMaterial();
        }

        @Override
        public boolean useAmbientOcclusion() {
            return source.useAmbientOcclusion();
        }
    }

    private record EldritchGeometryKey(long pos, Object delegateKey) {}
}
