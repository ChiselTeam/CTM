package io.github.chiselteam.ctm;

import io.github.chiselteam.ctm.impl.model.*;
import io.github.chiselteam.ctm.network.CTMBlockOffsetSyncPacket;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PortRegressionTest {
    @Test void memoCachesPerBakerAndToleratesNullChildren() {
        ModelBaker first = mock(ModelBaker.class), second = mock(ModelBaker.class);
        ModelBaker.Interner interner = mock(ModelBaker.Interner.class);
        when(first.interner()).thenReturn(interner);
        when(second.interner()).thenReturn(interner);
        BlockStateModel model = mock(BlockStateModel.class, withSettings().extraInterfaces(CTMQuadStorage.class));
        CTMQuadStorage storage = (CTMQuadStorage) model;
        doAnswer(call -> { ((java.util.function.Consumer<BlockStateModel>) call.getArgument(0)).accept(null); return null; }).when(storage).visitChildModels(any());
        doAnswer(call -> { ((java.util.function.Consumer<net.minecraft.client.resources.model.geometry.BakedQuad[]>) call.getArgument(0)).accept(null); return null; }).when(storage).visitQuadArrays(any());
        AtomicInteger calls = new AtomicInteger();
        var memo = new CTMBakeCache.Memo();
        assertSame(model, memo.bake(first, () -> { calls.incrementAndGet(); return model; }));
        assertSame(model, memo.bake(first, () -> { throw new AssertionError(); }));
        assertSame(model, memo.bake(second, () -> { calls.incrementAndGet(); return model; }));
        assertEquals(2, calls.get());
    }

    @Test void deferredMetadataDoesNotRealizeAndFirstUseRealizesOnce() {
        BlockStateModel model = mock(BlockStateModel.class);
        AtomicInteger calls = new AtomicInteger();
        var deferred = new DeferredCTMModel("test", null, () -> { calls.incrementAndGet(); return model; }, null);
        assertEquals(0, deferred.materialFlags());
        assertNull(deferred.particleMaterial());
        deferred.visitChildModels(child -> fail("Realized early"));
        assertEquals(0, calls.get());
        deferred.collectParts(null, new ArrayList<>());
        deferred.collectParts(null, new ArrayList<>());
        assertEquals(1, calls.get());
        deferred.visitChildModels(child -> assertSame(model, child));
    }

    @Test void packetBatchesReplaceOnlyFirstAndRetainEveryOffset() {
        Map<BlockPos, BlockPos> offsets = new HashMap<>();
        for (int i = 0; i < 8200; i++) offsets.put(new BlockPos(0, i, 0), new BlockPos(1, 2, 3));
        var packets = CTMBlockOffsetSyncPacket.batches(new ChunkPos(0, 0), offsets, true);
        assertEquals(2, packets.size());
        assertTrue(packets.getFirst().replace());
        assertFalse(packets.getLast().replace());
        assertEquals(8192, packets.getFirst().offsets().size());
        assertEquals(8200, packets.stream().mapToInt(p -> p.offsets().size()).sum());
        assertEquals(1, CTMBlockOffsetSyncPacket.batches(new ChunkPos(0, 0), Map.of(), true).size());
        assertTrue(CTMBlockOffsetSyncPacket.batches(new ChunkPos(0, 0), Map.of(), false).isEmpty());
    }
    @Test void snapshotBakerUsesCapturedMaterialsAndEmptyProperties() {
        var id = net.minecraft.resources.Identifier.fromNamespaceAndPath("ctm", "snapshot_test");
        var material = new net.minecraft.client.resources.model.sprite.Material(id, false);
        var sprite = mock(net.minecraft.client.renderer.texture.TextureAtlasSprite.class);
        var baked = new net.minecraft.client.resources.model.sprite.Material.Baked(sprite, false);
        var inputs = new DeferredCTMModel.Inputs(id, null, Map.of(material, baked), mock(ModelBaker.Interner.class));
        var resolved = inputs.getModel(id);
        assertSame(baked, inputs.materials().get(material, resolved));
        assertSame(net.minecraft.util.context.ContextMap.EMPTY, resolved.getTopAdditionalProperties());
        assertThrows(IllegalStateException.class, () -> inputs.materials().reportMissingReference("absent", resolved));
        assertThrows(IllegalStateException.class, () -> inputs.getModel(net.minecraft.resources.Identifier.fromNamespaceAndPath("ctm", "other")));
    }}
