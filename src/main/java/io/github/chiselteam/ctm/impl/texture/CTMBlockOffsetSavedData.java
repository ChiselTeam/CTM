package io.github.chiselteam.ctm.impl.texture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;

/** Per-dimension persistent offsets, indexed by chunk for watch synchronization. */
public final class CTMBlockOffsetSavedData extends SavedData {
    public record Entry(BlockPos pos, Block block, BlockPos offset) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(Entry::pos),
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(Entry::block),
                BlockPos.CODEC.fieldOf("offset").forGetter(Entry::offset)
        ).apply(i, Entry::new));
    }
    public static final Codec<CTMBlockOffsetSavedData> CODEC = Entry.CODEC.listOf().xmap(
            CTMBlockOffsetSavedData::new, CTMBlockOffsetSavedData::entries);
    public static final String ID = "ctm_block_offsets";
    public static final SavedData.Factory<CTMBlockOffsetSavedData> FACTORY =
            new SavedData.Factory<>(CTMBlockOffsetSavedData::new, CTMBlockOffsetSavedData::load, null);

    public static CTMBlockOffsetSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        Tag entries = tag.get("entries");
        return entries == null ? new CTMBlockOffsetSavedData() : CODEC.parse(NbtOps.INSTANCE, entries).getOrThrow();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("entries", CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow());
        return tag;
    }
    private final Map<ChunkPos, Map<BlockPos, Entry>> chunks = new HashMap<>();

    public CTMBlockOffsetSavedData() {}
    private CTMBlockOffsetSavedData(List<Entry> entries) {
        entries.forEach(entry -> putInternal(entry));
    }
    private List<Entry> entries() {
        return chunks.values().stream().flatMap(map -> map.values().stream()).toList();
    }
    private static ChunkPos chunkOf(BlockPos pos) {
        return new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
    }
    private void putInternal(Entry entry) {
        chunks.computeIfAbsent(chunkOf(entry.pos()), ignored -> new HashMap<>()).put(entry.pos(), entry);
    }
    public Entry get(BlockPos pos) {
        return chunks.getOrDefault(chunkOf(pos), Map.of()).get(pos);
    }
    public Collection<Entry> inChunk(ChunkPos chunk) {
        return List.copyOf(chunks.getOrDefault(chunk, Map.of()).values());
    }
    public void put(BlockPos pos, Block block, BlockPos offset) {
        Entry entry = new Entry(pos.immutable(), block, offset.immutable());
        if (!entry.equals(get(pos))) {
            putInternal(entry);
            setDirty();
        }
    }
    public void remove(BlockPos pos) {
        var entries = chunks.get(chunkOf(pos));
        if (entries != null && entries.remove(pos) != null) {
            if (entries.isEmpty()) chunks.remove(chunkOf(pos));
            setDirty();
        }
    }
}
