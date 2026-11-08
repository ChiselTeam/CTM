package io.github.chiselteam.ctm.impl.texture;

import io.github.chiselteam.ctm.test.VanillaBootstrap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CTMBlockOffsetSavedDataTest {
    @BeforeAll static void bootstrap() {
        VanillaBootstrap.initialize();
    }

    @Test void persistenceRetainsBlocksOffsetsAndChunkIndex() {
        var data = new CTMBlockOffsetSavedData();
        BlockPos first = new BlockPos(-1, -64, 15);
        BlockPos second = new BlockPos(16, 100, 16);
        data.put(first, Blocks.STONE, new BlockPos(-3, 2, 1));
        data.put(second, Blocks.DIRT, new BlockPos(4, 0, 0));
        var restored = CTMBlockOffsetSavedData.load(data.save(new CompoundTag(), null), null);
        assertEquals(data.get(first), restored.get(first));
        assertEquals(data.get(second), restored.get(second));
        assertEquals(1, restored.inChunk(new ChunkPos(-1, 0)).size());
        assertEquals(1, restored.inChunk(new ChunkPos(1, 1)).size());
        assertFalse(restored.isDirty());
        restored.remove(first);
        assertTrue(restored.isDirty());
        assertNull(restored.get(first));
        assertTrue(restored.inChunk(new ChunkPos(-1, 0)).isEmpty());
    }

    @Test void mutableInputsAreCopiedAndUnchangedWritesStayClean() {
        var data = new CTMBlockOffsetSavedData();
        var pos = new BlockPos.MutableBlockPos(1, 2, 3);
        var offset = new BlockPos.MutableBlockPos(4, 5, 6);
        data.put(pos, Blocks.STONE, offset);
        pos.set(10, 20, 30);
        offset.set(40, 50, 60);
        BlockPos original = new BlockPos(1, 2, 3);
        assertEquals(new BlockPos(4, 5, 6), data.get(original).offset());
        data.setDirty(false);
        data.put(original, Blocks.STONE, new BlockPos(4, 5, 6));
        assertFalse(data.isDirty());
        data.remove(pos);
        assertFalse(data.isDirty());
    }

    @Test void missingEntriesLoadAsEmpty() {
        assertTrue(CTMBlockOffsetSavedData.load(new CompoundTag(), null).inChunk(new ChunkPos(0, 0)).isEmpty());
    }
}
