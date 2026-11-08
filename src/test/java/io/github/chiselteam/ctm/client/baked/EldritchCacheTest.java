package io.github.chiselteam.ctm.client.baked;

import io.github.chiselteam.ctm.impl.model.DeferredCTMModel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import static org.mockito.Mockito.*;

class EldritchCacheTest {
    @Test void cachedDeferredGeometryRefreshesWhenDelegateKeyChanges() {
        DynamicBlockStateModel delegate = mock(DynamicBlockStateModel.class);
        BlockAndTintGetter level = mock(BlockAndTintGetter.class);
        BlockState state = mock(BlockState.class);
        RandomSource random = mock(RandomSource.class);
        AtomicReference<Object> key = new AtomicReference<>("initial");
        when(delegate.createGeometryKey(level, BlockPos.ZERO, state, random)).thenAnswer(call -> key.get());
        var model = new EldritchBlockStateModel(new DeferredCTMModel("test", null, () -> delegate, null));
        var parts = new ArrayList<BlockStateModelPart>();
        model.collectParts(level, BlockPos.ZERO, state, random, parts);
        model.collectParts(level, BlockPos.ZERO, state, random, parts);
        verify(delegate, times(1)).collectParts(eq(level), eq(BlockPos.ZERO), eq(state), eq(random), anyList());
        key.set("changed");
        model.collectParts(level, BlockPos.ZERO, state, random, parts);
        verify(delegate, times(2)).collectParts(eq(level), eq(BlockPos.ZERO), eq(state), eq(random), anyList());
        key.set(null);
        model.collectParts(level, BlockPos.ZERO, state, random, parts);
        model.collectParts(level, BlockPos.ZERO, state, random, parts);
        verify(delegate, times(4)).collectParts(eq(level), eq(BlockPos.ZERO), eq(state), eq(random), anyList());
    }
}
