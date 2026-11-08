package io.github.chiselteam.ctm.api.texture;

import io.github.chiselteam.ctm.test.VanillaBootstrap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CTMBlockOffsetsTest {
    @BeforeAll static void bootstrap() {
        VanillaBootstrap.initialize();
    }

    @Test void nonpositiveLimitsFailBeforeReadingOrMutatingLevel() {
        ServerLevel level = mock(ServerLevel.class);
        for (int limit : new int[]{0, -1}) {
            assertThrows(IllegalArgumentException.class,
                    () -> CTMBlockOffsets.setConnectedOffset(level, BlockPos.ZERO, BlockPos.ZERO, limit));
        }
        verifyNoInteractions(level);
    }

    @Test void exceededLimitFailsBeforeStorageOrSynchronization() {
        ServerLevel level = mock(ServerLevel.class);
        when(level.hasChunkAt(any(BlockPos.class))).thenReturn(true);
        when(level.getBlockState(any(BlockPos.class))).thenReturn(Blocks.STONE.defaultBlockState());
        assertThrows(IllegalArgumentException.class,
                () -> CTMBlockOffsets.setConnectedOffset(level, BlockPos.ZERO, new BlockPos(1, 0, 0), 1));
        verify(level, never()).getDataStorage();
        assertOnlyTraversalCalls(level);
    }

    @Test void unloadedOriginDoesNotReadBlockStateOrStorage() {
        ServerLevel level = mock(ServerLevel.class);
        assertTrue(CTMBlockOffsets.setConnectedOffset(level, BlockPos.ZERO, BlockPos.ZERO, 10).isEmpty());
        verify(level).hasChunkAt(BlockPos.ZERO);
        verifyNoMoreInteractions(level);
    }

    @Test void traversalIgnoresStatePropertiesAndDoesNotReadUnloadedNeighbors() {
        BlockPos origin = new BlockPos(15, 64, 0);
        BlockPos sameBlock = origin.west();
        BlockPos differentBlock = origin.south();
        Map<BlockPos, BlockState> loaded = Map.of(
                origin, Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X),
                sameBlock, Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z),
                differentBlock, Blocks.BIRCH_LOG.defaultBlockState());
        ServerLevel level = mock(ServerLevel.class);
        when(level.hasChunkAt(any(BlockPos.class))).thenAnswer(call -> {
            BlockPos pos = call.getArgument(0);
            return (pos.getX() >> 4) == 0 && (pos.getZ() >> 4) == 0;
        });
        when(level.getBlockState(any(BlockPos.class))).thenAnswer(call -> {
            BlockPos pos = call.getArgument(0);
            assertTrue((pos.getX() >> 4) == 0 && (pos.getZ() >> 4) == 0,
                    "Attempted unloaded state read at " + pos);
            return loaded.getOrDefault(pos, Blocks.AIR.defaultBlockState());
        });
        assertEquals(Set.of(origin, sameBlock), CTMBlockOffsets.findConnected(level, origin, 2));
        verify(level, never()).getDataStorage();
        assertOnlyTraversalCalls(level);
    }

    private static void assertOnlyTraversalCalls(ServerLevel level) {
        mockingDetails(level).getInvocations().forEach(call -> assertTrue(
                Set.of("hasChunkAt", "getBlockState").contains(call.getMethod().getName()),
                "Unexpected loading or mutation call: " + call.getMethod().getName()));
    }
}
