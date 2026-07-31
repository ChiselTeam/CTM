package io.github.chiselteam.ctm.client;

import io.github.chiselteam.ctm.api.geometry.CTMKeyWithOverlays;
import io.github.chiselteam.ctm.api.model.ConnectedTextureBlockModelPart;
import io.github.chiselteam.ctm.api.model.CTMOverlayRule;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractConnectedTextureBlockStateModel<K> implements DynamicBlockStateModel {

    protected final Set<Direction> connectedFaces;
    protected final Set<Direction> unculledFaces;
    protected final boolean renderOverlayOnAllFaces;
    protected final Map<Direction, BakedQuad[]> baseQuads;
    protected final TextureAtlasSprite particle;
    protected final CTMVariant variant;
    protected final Material.Baked particleMaterial;
    protected final CTMBlockPredicate connectionPredicate;
    protected final List<CTMOverlayRule> overlayRules;

    private final Map<Object, ConnectedTextureBlockModelPart> parts = new ConcurrentHashMap<>();

    protected final int materialFlags;

    protected AbstractConnectedTextureBlockStateModel(Set<Direction> connectedFaces, Set<Direction> unculledFaces, boolean renderOverlayOnAllFaces, Map<Direction, BakedQuad[]> baseQuads, TextureAtlasSprite particle, CTMVariant variant, CTMBlockPredicate connectionPredicate, List<CTMOverlayRule> overlayRules, int materialFlags) {
        this.connectedFaces = connectedFaces;
        this.unculledFaces = unculledFaces;
        this.renderOverlayOnAllFaces = renderOverlayOnAllFaces;
        this.baseQuads = baseQuads;
        this.particle = particle;
        this.variant = variant;
        this.particleMaterial = new Material.Baked(particle, false);
        this.connectionPredicate = connectionPredicate;
        this.overlayRules = overlayRules;
        this.materialFlags = materialFlags;
    }

    protected AbstractConnectedTextureBlockStateModel(Set<Direction> connectedFaces, Set<Direction> unculledFaces, boolean renderOverlayOnAllFaces, Map<Direction, BakedQuad[]> baseQuads, TextureAtlasSprite particle, CTMVariant variant, CTMBlockPredicate connectionPredicate, List<CTMOverlayRule> overlayRules) {
        this(connectedFaces, unculledFaces, renderOverlayOnAllFaces, baseQuads, particle, variant, connectionPredicate, overlayRules, computeFlags(baseQuads));
    }

    private static int computeFlags(Map<Direction, BakedQuad[]> baseQuads) {
        int flags = 0;
        for (BakedQuad[] quads : baseQuads.values()) {
            for (BakedQuad quad : quads) {
                if (quad != null) {
                    flags |= quad.materialInfo().flags();
                }
            }
        }
        return flags;
    }

    protected AbstractConnectedTextureBlockStateModel(Set<Direction> connectedFaces, Set<Direction> unculledFaces, boolean renderOverlayOnAllFaces, Map<Direction, BakedQuad[]> baseQuads, TextureAtlasSprite particle, CTMVariant variant) {
        this(connectedFaces, unculledFaces, renderOverlayOnAllFaces, baseQuads, particle, variant, CTMBlockPredicate.sameBlock(), List.of());
    }

    @Override
    public @NonNull Object createGeometryKey(@NonNull BlockAndTintGetter level, @NonNull BlockPos pos, @NonNull BlockState state, @NonNull RandomSource random) {
        K baseKey = computeCTMKey(level, pos, state, random);
        long mask = computeOverlayMask(level, pos, state);
        Object key = mask == 0 ? baseKey : new CTMKeyWithOverlays<>(baseKey, mask);
        return new GeometryKey(this, key);
    }

    @Override
    public void collectParts(@NonNull BlockAndTintGetter level, @NonNull BlockPos pos, @NonNull BlockState state, @NonNull RandomSource random, @NonNull List<BlockStateModelPart> parts) {
        K baseKey = computeCTMKey(level, pos, state, random);
        long mask = computeOverlayMask(level, pos, state);
        Object key = mask == 0 ? baseKey : new CTMKeyWithOverlays<>(baseKey, mask);
        parts.add(this.parts.computeIfAbsent(key, k -> {
            if (k instanceof CTMKeyWithOverlays) {
                return createPart(((CTMKeyWithOverlays<K>) k).baseKey(), ((CTMKeyWithOverlays<K>) k).overlayMask());
            } else {
                return createPart((K) k, 0L);
            }
        }));
    }

    protected abstract K computeCTMKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random);

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
    public Material.@NonNull Baked particleMaterial() {
        return particleMaterial;
    }

    @Override
    public int materialFlags() {
        return materialFlags;
    }
}
