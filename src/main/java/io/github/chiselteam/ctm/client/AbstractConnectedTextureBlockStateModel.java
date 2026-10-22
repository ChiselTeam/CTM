package io.github.chiselteam.ctm.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.github.chiselteam.ctm.api.geometry.CTMKeyWithOverlays;
import io.github.chiselteam.ctm.api.model.CTMOverlayRule;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.model.ConnectedTextureBlockModelPart;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.client.model.BakedTexture;
import io.github.chiselteam.ctm.client.model.CTMBakedModel;
import io.github.chiselteam.ctm.client.model.CTMItemView;
import io.github.chiselteam.ctm.client.model.CTMModelPart;
import io.github.chiselteam.ctm.impl.model.CTMQuadStorage;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public abstract class AbstractConnectedTextureBlockStateModel<K> implements CTMBakedModel, CTMQuadStorage {

    private static final int MAX_CACHED_PARTS = 256;

    protected final Set<Direction> connectedFaces;
    protected final Set<Direction> unculledFaces;
    protected final boolean renderOverlayOnAllFaces;
    protected final Map<Direction, BakedQuad[]> baseQuads;
    protected final TextureAtlasSprite particle;
    protected final CTMVariant variant;
    protected final BakedTexture particleMaterial;
    protected final CTMBlockPredicate connectionPredicate;
    protected final List<CTMOverlayRule> overlayRules;
    protected final boolean ambientOcclusion;

    private volatile LoadingCache<Object, ConnectedTextureBlockModelPart> parts;

    protected AbstractConnectedTextureBlockStateModel(Set<Direction> connectedFaces, Set<Direction> unculledFaces, boolean renderOverlayOnAllFaces, Map<Direction, BakedQuad[]> baseQuads, TextureAtlasSprite particle, CTMVariant variant, CTMBlockPredicate connectionPredicate, List<CTMOverlayRule> overlayRules, boolean ambientOcclusion) {
        this.connectedFaces = connectedFaces;
        this.unculledFaces = unculledFaces;
        this.renderOverlayOnAllFaces = renderOverlayOnAllFaces;
        this.baseQuads = baseQuads;
        this.particle = particle;
        this.variant = variant;
        this.particleMaterial = new BakedTexture(particle, false);
        this.connectionPredicate = connectionPredicate;
        this.overlayRules = overlayRules;
        this.ambientOcclusion = ambientOcclusion;
    }

    protected AbstractConnectedTextureBlockStateModel(Set<Direction> connectedFaces, Set<Direction> unculledFaces, boolean renderOverlayOnAllFaces, Map<Direction, BakedQuad[]> baseQuads, TextureAtlasSprite particle, CTMVariant variant, CTMBlockPredicate connectionPredicate, List<CTMOverlayRule> overlayRules) {
        this(connectedFaces, unculledFaces, renderOverlayOnAllFaces, baseQuads, particle, variant, connectionPredicate, overlayRules, true);
    }

    protected AbstractConnectedTextureBlockStateModel(Set<Direction> connectedFaces, Set<Direction> unculledFaces, boolean renderOverlayOnAllFaces, Map<Direction, BakedQuad[]> baseQuads, TextureAtlasSprite particle, CTMVariant variant) {
        this(connectedFaces, unculledFaces, renderOverlayOnAllFaces, baseQuads, particle, variant, CTMBlockPredicate.sameBlock(), List.of());
    }

    @Override
    public @NotNull Object createGeometryKey(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull RandomSource random) {
        K baseKey = computeCTMKey(level, pos, state, random);
        long mask = computeOverlayMask(level, pos, state);
        Object key = mask == 0 ? baseKey : new CTMKeyWithOverlays<>(baseKey, mask);
        return new GeometryKey(this, key);
    }

    @Override
    public void collectParts(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull RandomSource random, @NotNull List<CTMModelPart> parts) {
        K baseKey = computeCTMKey(level, pos, state, random);
        long mask = computeOverlayMask(level, pos, state);
        Object key = mask == 0 ? baseKey : new CTMKeyWithOverlays<>(baseKey, mask);
        parts.add(partCache().getUnchecked(key));
    }

    private LoadingCache<Object, ConnectedTextureBlockModelPart> partCache() {
        var cache = parts;
        if (cache == null) {
            synchronized (this) {
                cache = parts;
                if (cache == null) {
                    cache = CacheBuilder.newBuilder()
                            .maximumSize(MAX_CACHED_PARTS)
                            .build(CacheLoader.from(this::createCachedPart));
                    parts = cache;
                }
            }
        }
        return cache;
    }

    @SuppressWarnings("unchecked")
    private ConnectedTextureBlockModelPart createCachedPart(Object key) {
        if (key instanceof CTMKeyWithOverlays<?>(Object baseKey, long overlayMask)) {
            return createPart((K) baseKey, overlayMask);
        }
        return createPart((K) key, 0L);
    }

    @Override
    public void visitQuadArrays(Consumer<BakedQuad[]> visitor) {
        baseQuads.values().forEach(visitor);
    }

    protected abstract K computeCTMKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random);

    @Override
    public void collectParts(RandomSource random, List<CTMModelPart> parts) {
        BlockState state = variant.targetBlock().defaultBlockState();
        var view = new CTMItemView(state);
        parts.add(partCache().getUnchecked(computeCTMKey(view, BlockPos.ZERO, state, random)));
    }

    @Override
    public boolean useAmbientOcclusion() {
        return ambientOcclusion;
    }

    protected abstract ConnectedTextureBlockModelPart createPart(K key, long overlayMask);

    protected long computeOverlayMask(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        long mask = 0;
        int ruleCount = Math.min(overlayRules.size(), 10);
        for (int i = 0; i < ruleCount; i++) {
            CTMOverlayRule rule = overlayRules.get(i);
            for (Direction face : Direction.values()) {
                if (rule.test(level, pos, state, face)) {
                    mask |= (1L << (i * 6 + face.ordinal()));
                }
            }
        }
        return mask;
    }

    protected boolean shouldConnectSide(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction face, Direction side) {
        BlockPos neighborPos = pos.relative(side);
        return matches(level, pos, state, face, neighborPos);
    }

    protected boolean isCornerBlockPresent(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction face, Direction side1, Direction side2) {
        BlockPos neighborPos = pos.relative(side1).relative(side2);
        return matches(level, pos, state, face, neighborPos);
    }

    protected final boolean matches(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction face, BlockPos neighborPos) {
        BlockState neighborState = level.getBlockState(neighborPos);
        BlockState localAppearance = state.getAppearance(level, pos, face, neighborState, neighborPos);
        if (localAppearance.is(Blocks.AIR) || !localAppearance.is(variant.targetBlock())) return false;

        BlockState neighborAppearance = neighborState.getAppearance(level, neighborPos, face, state, pos);
        if (neighborAppearance.is(Blocks.AIR)) return false;

        return connectionPredicate.test(variant.targetBlock(), neighborAppearance);
    }

    public record GeometryKey(AbstractConnectedTextureBlockStateModel<?> model, Object key) { }

    @Override
    public BakedTexture particleMaterial() {
        return particleMaterial;
    }

}
