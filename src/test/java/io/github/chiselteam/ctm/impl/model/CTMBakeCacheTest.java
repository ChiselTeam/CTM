package io.github.chiselteam.ctm.impl.model;

import com.mojang.math.Transformation;
import io.github.chiselteam.ctm.client.model.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CTMBakeCacheTest {
    @Test void structurallyEqualQuadsShareOnlyInsideTheirScope() {
        var sprite = mock(TextureAtlasSprite.class);
        var scope = new CTMBakeCache.Scope();
        var original = quad(sprite, 2, true, true, Direction.UP, 7);
        assertSame(original, scope.quad(original));
        assertSame(original, scope.quad(quad(sprite, 2, true, true, Direction.UP, 7)));
        for (var changed : List.of(quad(sprite, 3, true, true, Direction.UP, 7),
                quad(sprite, 2, false, true, Direction.UP, 7), quad(sprite, 2, true, false, Direction.UP, 7),
                quad(sprite, 2, true, true, Direction.DOWN, 7), quad(sprite, 2, true, true, Direction.UP, 8),
                quad(mock(TextureAtlasSprite.class), 2, true, true, Direction.UP, 7))) {
            assertSame(changed, scope.quad(changed));
        }
        var other = quad(sprite, 2, true, true, Direction.UP, 7);
        assertSame(other, new CTMBakeCache.Scope().quad(other));
    }

    @Test void traversalHandlesNullArraysNullChildrenAndCyclesWithoutRealizingDeferredChildren() {
        var sprite = mock(TextureAtlasSprite.class);
        var first = new Node();
        var second = new Node();
        first.quads = new BakedQuad[]{quad(sprite, 1, true, false, Direction.NORTH, 4), null};
        second.quads = new BakedQuad[]{quad(sprite, 1, true, false, Direction.NORTH, 4)};
        var calls = new AtomicInteger();
        var scope = new CTMBakeCache.Scope();
        var deferred = new DeferredCTMModel("test", null, true, () -> { calls.incrementAndGet(); return new Node(); }, scope);
        first.children.addAll(Arrays.asList(second, null, first, deferred));
        second.children.add(first);
        scope.finish(first);
        assertSame(first.quads[0], second.quads[0]);
        assertNull(first.quads[1]);
        assertEquals(1, first.visits);
        assertEquals(1, second.visits);
        assertEquals(0, calls.get());
    }

    @Test void memoReusesEquivalentStatesButInvalidatesEveryBakeInput() {
        var baker = mock(ModelBaker.class);
        var geometry = mock(IGeometryBakingContext.class);
        Function<Material, TextureAtlasSprite> sprites = material -> null;
        var identity = CTMModelState.of(Transformation.identity(), false);
        var original = new CTMBakingContext(baker, sprites, identity, geometry, true);
        var memo = new CTMBakeCache.Memo();
        Supplier<CTMBakedModel> factory = Node::new;
        var model = memo.bake(original, factory);
        assertSame(model, memo.bake(new CTMBakingContext(baker, sprites,
                CTMModelState.of(Transformation.identity(), false), geometry, true), factory));
        var changes = List.of(
                new CTMBakingContext(mock(ModelBaker.class), sprites, identity, geometry, true),
                new CTMBakingContext(baker, material -> null, identity, geometry, true),
                new CTMBakingContext(baker, sprites, identity, mock(IGeometryBakingContext.class), true),
                new CTMBakingContext(baker, sprites, identity, geometry, false),
                new CTMBakingContext(baker, sprites, CTMModelState.of(Transformation.identity(), true), geometry, true),
                new CTMBakingContext(baker, sprites, CTMModelState.of(new Transformation(new Matrix4f().rotateY(1)), false), geometry, true));
        for (var changed : changes) {
            var baseline = memo.bake(original, factory);
            assertNotSame(baseline, memo.bake(changed, factory));
        }
        assertSame(CTMBakeCache.scope(original), CTMBakeCache.scope(changes.getFirst()));
        assertNotSame(CTMBakeCache.scope(original), CTMBakeCache.scope(changes.get(1)));
    }

    @Test void concurrentMemoCallsPublishOneModel() throws Exception {
        var context = new CTMBakingContext(null, material -> null,
                CTMModelState.of(Transformation.identity(), false), null, true);
        var memo = new CTMBakeCache.Memo();
        var calls = new AtomicInteger();
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(8)) {
            var futures = new ArrayList<Future<CTMBakedModel>>();
            for (int i = 0; i < 16; i++) futures.add(pool.submit(() -> {
                start.await();
                return memo.bake(context, () -> { calls.incrementAndGet(); return new Node(); });
            }));
            start.countDown();
            var expected = futures.getFirst().get(10, TimeUnit.SECONDS);
            for (var future : futures) assertSame(expected, future.get(10, TimeUnit.SECONDS));
        }
        assertEquals(1, calls.get());
    }

    static BakedQuad quad(TextureAtlasSprite sprite, int tint, boolean shade, boolean ao, Direction face, int value) {
        var vertices = new int[32];
        Arrays.fill(vertices, value);
        return new BakedQuad(vertices, tint, face, sprite, shade, ao);
    }

    static class Node implements CTMBakedModel, CTMQuadStorage {
        BakedQuad[] quads;
        List<CTMBakedModel> children = new ArrayList<>();
        int visits;
        @Override public void visitQuadArrays(Consumer<BakedQuad[]> visitor) { visits++; visitor.accept(quads); }
        @Override public void visitChildModels(Consumer<CTMBakedModel> visitor) { children.forEach(visitor); }
        @Override public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) { return this; }
        @Override public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<CTMModelPart> parts) {}
        @Override public BakedTexture particleMaterial() { return null; }
    }
}
