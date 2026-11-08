package io.github.chiselteam.ctm.client;

import io.github.chiselteam.ctm.api.texture.MultiblockOffsetProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;

import java.util.Map;

public class CTMOffsetClientHandler {

    private static final Map<ChunkPos, BlockPos> chunkOffsets = new ConcurrentHashMap<>();

    private static final Map<BlockPos, BlockEntry> blockOffsets = new ConcurrentHashMap<>();
    private record BlockEntry(Block block, BlockPos offset) {}
    private static ClientLevel currentLevel;

    private static void checkLevel() {
        ClientLevel level = Minecraft.getInstance().level;
        if (currentLevel != level) {
            blockOffsets.clear();
            chunkOffsets.clear();
            currentLevel = level;
        }
    }

    public static void setBlockOffsets(ChunkPos chunk, Map<BlockPos, BlockPos> offsets, boolean replace) {
        checkLevel();
        if (currentLevel == null) return;
        var affected = updateBlockOffsets(chunk, offsets, replace, pos -> currentLevel.getBlockState(pos).getBlock());
        var sections = new java.util.HashSet<net.minecraft.core.SectionPos>();
        affected.forEach(pos -> sections.add(net.minecraft.core.SectionPos.of(pos)));
        sections.forEach(section -> currentLevel.setSectionRangeDirty(
                section.x(), section.y(), section.z(), section.x(), section.y(), section.z()));
    }

    // Return removed positions too: an empty replacement must rebuild old geometry.
    static java.util.Set<BlockPos> updateBlockOffsets(ChunkPos chunk, Map<BlockPos, BlockPos> offsets,
            boolean replace, java.util.function.Function<BlockPos, Block> blockAt) {
        var affected = new java.util.HashSet<BlockPos>();
        if (replace) blockOffsets.keySet().removeIf(pos -> {
            if ((pos.getX() >> 4) != chunk.x() || (pos.getZ() >> 4) != chunk.z()) return false;
            affected.add(pos);
            return true;
        });
        offsets.forEach((pos, offset) -> {
            affected.add(pos.immutable());
            if (offset.equals(BlockPos.ZERO)) blockOffsets.remove(pos);
            else blockOffsets.put(pos.immutable(), new BlockEntry(blockAt.apply(pos), offset.immutable()));
        });
        return affected;
    }

    static {
        MultiblockOffsetProvider.set(pos -> {
            checkLevel();
            BlockEntry entry = blockOffsets.get(pos);
            if (entry != null && currentLevel != null && currentLevel.getBlockState(pos).is(entry.block())) return pos.offset(entry.offset());
            ChunkPos chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
            BlockPos offset = getOffset(chunkPos);
            return pos.offset(offset);
        });
    }

    public static void setOffset(ChunkPos pos, BlockPos offset) {
        checkLevel();
        chunkOffsets.put(pos, offset);
    }

    public static BlockPos getOffset(ChunkPos pos) {
        return chunkOffsets.getOrDefault(pos, BlockPos.ZERO);
    }
}
