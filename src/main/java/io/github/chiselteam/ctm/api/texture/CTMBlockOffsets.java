package io.github.chiselteam.ctm.api.texture;

import io.github.chiselteam.ctm.network.CTMBlockOffsetSyncPacket;
import io.github.chiselteam.ctm.impl.texture.CTMBlockOffsetSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public final class CTMBlockOffsets {
    private CTMBlockOffsets() {}
    private static CTMBlockOffsetSavedData data(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(CTMBlockOffsetSavedData.FACTORY, CTMBlockOffsetSavedData.ID);
    }
    public static final int DEFAULT_LIMIT = 65536;

    public static BlockPos getOffset(ServerLevel level, BlockPos pos) {
        var entry = data(level).get(pos);
        return entry != null && level.getBlockState(pos).is(entry.block()) ? entry.offset() : BlockPos.ZERO;
    }

    /** Sets one block's absolute offset and synchronizes it. ZERO resets the block offset. */
    public static void setOffset(ServerLevel level, BlockPos pos, BlockPos offset) {
        apply(level, List.of(pos.immutable()), offset.immutable());
    }

    public static Set<BlockPos> setConnectedOffset(ServerLevel level, BlockPos origin, BlockPos offset) {
        return setConnectedOffset(level, origin, offset, DEFAULT_LIMIT);
    }

    /** Matches block type, ignoring state properties. Traverses six face neighbors in loaded chunks
     * only, without loading terrain. Throws before mutation if maxBlocks would be exceeded.
     * Returns the affected positions. Call on the server thread. */
    public static Set<BlockPos> setConnectedOffset(ServerLevel level, BlockPos origin, BlockPos offset, int maxBlocks) {
        Set<BlockPos> connected = findConnected(level, origin, maxBlocks);
        if (!connected.isEmpty()) apply(level, connected, offset.immutable());
        return connected;
    }

    static Set<BlockPos> findConnected(ServerLevel level, BlockPos origin, int maxBlocks) {
        if (maxBlocks < 1) throw new IllegalArgumentException("maxBlocks must be positive");
        if (!level.hasChunkAt(origin) || level.getBlockState(origin).isAir()) return Set.of();
        Block block = level.getBlockState(origin).getBlock();
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> connected = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        pending.add(origin.immutable());
        while (!pending.isEmpty()) {
            BlockPos pos = pending.removeFirst();
            if (!visited.add(pos) || !level.hasChunkAt(pos) || !level.getBlockState(pos).is(block)) continue;
            if (connected.size() == maxBlocks) throw new IllegalArgumentException("Connected block limit exceeded: " + maxBlocks);
            connected.add(pos);
            for (Direction direction : Direction.values()) pending.addLast(pos.relative(direction));
        }
        return Set.copyOf(connected);
    }

    private static void apply(ServerLevel level, Collection<BlockPos> positions, BlockPos offset) {
        CTMBlockOffsetSavedData entries = data(level);
        Map<ChunkPos, Map<BlockPos, BlockPos>> updates = new HashMap<>();
        for (BlockPos pos : positions) {
            if (!level.hasChunkAt(pos)) continue;
            if (offset.equals(BlockPos.ZERO)) entries.remove(pos);
            else entries.put(pos, level.getBlockState(pos).getBlock(), offset);
            updates.computeIfAbsent(new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4), ignored -> new HashMap<>()).put(pos, offset);
        }
        updates.forEach((chunk, values) -> CTMBlockOffsetSyncPacket.batches(chunk, values, false).forEach(
                packet -> PacketDistributor.sendToPlayersTrackingChunk(level, chunk, packet)));
    }

    /** Snapshot of valid offsets in a loaded chunk for watch synchronization. */
    public static Map<BlockPos, BlockPos> getOffsets(ServerLevel level, ChunkPos chunk) {
        Map<BlockPos, BlockPos> result = new HashMap<>();
        for (var entry : data(level).inChunk(chunk)) {
            if (level.getBlockState(entry.pos()).is(entry.block())) result.put(entry.pos(), entry.offset());
        }
        return Map.copyOf(result);
    }
}
