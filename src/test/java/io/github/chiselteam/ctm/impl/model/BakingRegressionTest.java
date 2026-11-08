package io.github.chiselteam.ctm.impl.model;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BakingRegressionTest {
    @Test void memoHandlesNullChildrenArraysAndCyclesAndIsScopedToBaker() {
        ModelBaker baker = mock(ModelBaker.class);
        when(baker.interner()).thenReturn(mock(ModelBaker.Interner.class));
        BlockStateModel model = mock(BlockStateModel.class, withSettings().extraInterfaces(CTMQuadStorage.class));
        CTMQuadStorage storage = (CTMQuadStorage) model;
        doAnswer(call -> {
            java.util.function.Consumer<BlockStateModel> visitor = call.getArgument(0);
            visitor.accept(null); visitor.accept(model); return null;
        }).when(storage).visitChildModels(any());
        doAnswer(call -> {
            java.util.function.Consumer<BakedQuad[]> visitor = call.getArgument(0);
            visitor.accept(null); visitor.accept(new BakedQuad[]{null}); return null;
        }).when(storage).visitQuadArrays(any());
        AtomicInteger calls = new AtomicInteger();
        CTMBakeCache.Memo memo = new CTMBakeCache.Memo();
        assertSame(model, memo.bake(baker, () -> { calls.incrementAndGet(); return model; }));
        assertSame(model, memo.bake(baker, () -> { fail("Repeated bake"); return null; }));
        ModelBaker next = mock(ModelBaker.class);
        when(next.interner()).thenReturn(mock(ModelBaker.Interner.class));
        memo.bake(next, () -> { calls.incrementAndGet(); return model; });
        assertEquals(2, calls.get());
    }

    @Test void deferredModelDoesNotBakeForMetadataAndRealizesOnce() {
        BlockStateModel model = mock(BlockStateModel.class);
        Material.Baked particle = new Material.Baked(mock(TextureAtlasSprite.class), false);
        when(model.particleMaterial()).thenReturn(particle);
        AtomicInteger calls = new AtomicInteger();
        DeferredCTMModel deferred = new DeferredCTMModel("test", particle, () -> { calls.incrementAndGet(); return model; }, null);
        assertSame(particle, deferred.particleMaterial());
        assertEquals(0, deferred.materialFlags());
        deferred.visitChildModels(child -> fail("Premature realization"));
        assertEquals(0, calls.get());
        deferred.collectParts(mock(RandomSource.class), new ArrayList<>());
        deferred.collectParts(mock(RandomSource.class), new ArrayList<>());
        assertEquals(1, calls.get());
        deferred.visitChildModels(child -> assertSame(model, child));
    }

    @Test void deferredModelRejectsMetadataMismatch() {
        BlockStateModel model = mock(BlockStateModel.class);
        when(model.materialFlags()).thenReturn(1);
        DeferredCTMModel deferred = new DeferredCTMModel("test", null, () -> model, null);
        assertThrows(IllegalStateException.class, () -> deferred.collectParts(mock(RandomSource.class), new ArrayList<>()));
    }

    @Test void materialSnapshotUsesCapturedValuesAndRejectsUncapturedReferences() {
        Material material = new Material(Identifier.fromNamespaceAndPath("ctm", "test"), false);
        Material.Baked baked = new Material.Baked(mock(TextureAtlasSprite.class), false);
        DeferredCTMModel.Inputs inputs = new DeferredCTMModel.Inputs(Identifier.fromNamespaceAndPath("ctm", "model"),
                mock(TextureSlots.class), Map.of(material, baked), mock(ModelBaker.Interner.class));
        ModelDebugName name = () -> "test";
        assertSame(baked, inputs.materials().get(material, name));
        assertSame(baked, inputs.materials().replacementForMissingMaterial(material));
        assertThrows(IllegalStateException.class, () -> inputs.materials().reportMissingReference("missing", name));
        assertThrows(NullPointerException.class, () -> inputs.materials().get(new Material(Identifier.fromNamespaceAndPath("ctm", "other"), false), name));
    }
}
