package io.github.chiselteam.ctm.impl.model;

import io.github.chiselteam.ctm.client.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeferredCTMModelTest {
    @Test void metadataAndTraversalDoNotBakeAndFirstUsePublishesExactlyOnce() throws Exception {
        var particle = new BakedTexture(mock(TextureAtlasSprite.class), false);
        var delegate = mock(CTMBakedModel.class);
        when(delegate.particleMaterial()).thenReturn(particle);
        when(delegate.useAmbientOcclusion()).thenReturn(false);
        when(delegate.createGeometryKey(null, null, null, null)).thenReturn("key");
        var calls = new AtomicInteger();
        var deferred = new DeferredCTMModel("test", particle, false,
                () -> { calls.incrementAndGet(); return delegate; }, new CTMBakeCache.Scope());
        assertSame(particle, deferred.particleMaterial());
        assertSame(particle.sprite(), deferred.getParticleIcon());
        assertFalse(deferred.useAmbientOcclusion());
        deferred.visitChildModels(child -> fail("Unrealized child leaked"));
        new CTMBakeCache.Scope().finish(deferred);
        assertEquals(0, calls.get());
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(8)) {
            var futures = new ArrayList<Future<Object>>();
            for (int i = 0; i < 24; i++) futures.add(pool.submit(() -> {
                start.await();
                return deferred.createGeometryKey(null, null, null, null);
            }));
            start.countDown();
            for (var future : futures) assertEquals("key", future.get(10, TimeUnit.SECONDS));
        }
        assertEquals(1, calls.get());
        var children = new ArrayList<CTMBakedModel>();
        deferred.visitChildModels(children::add);
        assertEquals(List.of(delegate), children);
        var parts = new ArrayList<CTMModelPart>();
        deferred.collectParts(null, parts);
        deferred.collectParts(null, null, null, null, parts);
        verify(delegate).collectParts(null, parts);
        verify(delegate).collectParts(null, null, null, null, parts);
    }

    @Test void mismatchedParticleOrAmbientOcclusionCannotBePublished() {
        var particle = new BakedTexture(mock(TextureAtlasSprite.class), false);
        var delegate = mock(CTMBakedModel.class);
        var scope = new CTMBakeCache.Scope();
        var wrongParticle = new DeferredCTMModel("particle", particle, false, () -> delegate, scope);
        assertThrows(IllegalStateException.class, () -> wrongParticle.collectParts(null, new ArrayList<>()));
        wrongParticle.visitChildModels(child -> fail("Invalid model published"));
        when(delegate.particleMaterial()).thenReturn(particle);
        var wrongAo = new DeferredCTMModel("ao", particle, true, () -> delegate, scope);
        assertThrows(IllegalStateException.class, () -> wrongAo.collectParts(null, new ArrayList<>()));
        wrongAo.visitChildModels(child -> fail("Invalid model published"));
    }
}
