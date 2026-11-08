package io.github.chiselteam.ctm.impl.texture;

import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CTMBlockOffsetSavedDataTest {
    @BeforeAll static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }
    @Test void copiesMutablePositionsAndMaintainsChunkIndex() {
        CTMBlockOffsetSavedData data = new CTMBlockOffsetSavedData();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(-1, 20, -1);
        BlockPos.MutableBlockPos offset = new BlockPos.MutableBlockPos(2, 3, 4);
        data.put(pos, Blocks.STONE, offset);
        pos.set(20, 20, 20); offset.set(0, 0, 0);
        BlockPos original = new BlockPos(-1, 20, -1);
        assertEquals(new BlockPos(2, 3, 4), data.get(original).offset());
        var snapshot = data.inChunk(new ChunkPos(-1, -1));
        assertEquals(1, snapshot.size());
        assertTrue(data.isDirty());
        data.remove(original);
        assertNull(data.get(original));
        assertTrue(data.inChunk(new ChunkPos(-1, -1)).isEmpty());
        assertEquals(1, snapshot.size());
    }
    @Test void codecPreservesBlockIdentityAndOffsetsAcrossReload() {
        CTMBlockOffsetSavedData data = new CTMBlockOffsetSavedData();
        BlockPos pos = new BlockPos(18, -10, -19);
        data.put(pos, Blocks.STONE, new BlockPos(-2, 4, 1));
        var json = CTMBlockOffsetSavedData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        var restored = CTMBlockOffsetSavedData.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        assertEquals(data.get(pos), restored.get(pos));
        assertEquals(1, restored.inChunk(new ChunkPos(1, -2)).size());
    }
}
