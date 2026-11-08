package io.github.chiselteam.ctm.impl.model;

import io.github.chiselteam.ctm.client.baked.EldritchBlockStateModel;
import io.github.chiselteam.ctm.client.model.*;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EldritchCacheTest {
    @Test void trustedDeferredDelegateCachesAndInvalidatesGeometryKeysAndPositions() {
        var delegate = mock(CTMBakedModel.class);
        when(delegate.useAmbientOcclusion()).thenReturn(true);
        when(delegate.createGeometryKey(any(), any(), any(), any())).thenReturn("first");
        var wrapped = new DeferredCTMModel("test", null, true, () -> delegate, new CTMBakeCache.Scope());
        var model = new EldritchBlockStateModel(wrapped);
        var pos = new BlockPos(2, 3, 4);
        collect(model, pos); collect(model, pos);
        verify(delegate, times(1)).collectParts(isNull(), eq(pos), isNull(), isNull(), anyList());
        when(delegate.createGeometryKey(any(), any(), any(), any())).thenReturn("second");
        collect(model, pos); collect(model, pos);
        verify(delegate, times(2)).collectParts(isNull(), eq(pos), isNull(), isNull(), anyList());
        var other = pos.above();
        collect(model, other);
        assertNotEquals(model.createGeometryKey(null, pos, null, null), model.createGeometryKey(null, other, null, null));
        when(delegate.createGeometryKey(any(), any(), any(), any())).thenReturn(null);
        assertNull(model.createGeometryKey(null, pos, null, null));
        collect(model, pos); collect(model, pos);
        verify(delegate, times(4)).collectParts(isNull(), eq(pos), isNull(), isNull(), anyList());
    }

    @Test void unknownDelegatesAreNeverCachedEvenWhenTheyReturnAStableKey() {
        var delegate = mock(CTMBakedModel.class);
        when(delegate.createGeometryKey(any(), any(), any(), any())).thenReturn("constant");
        var model = new EldritchBlockStateModel(delegate);
        collect(model, BlockPos.ZERO); collect(model, BlockPos.ZERO);
        verify(delegate, times(2)).collectParts(isNull(), eq(BlockPos.ZERO), isNull(), isNull(), anyList());
        verify(delegate, never()).createGeometryKey(any(), any(), any(), any());
    }

    private static void collect(EldritchBlockStateModel model, BlockPos pos) {
        model.collectParts(null, pos, null, null, new ArrayList<>());
    }
}
