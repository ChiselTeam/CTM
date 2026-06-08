package io.github.chiselteam.ctm.api.texture;

import net.minecraft.core.BlockPos;

@FunctionalInterface
public interface MultiblockOffsetProvider {

    MultiblockOffsetProvider NONE = pos -> pos;

    BlockPos offsetFor(BlockPos pos);

    static MultiblockOffsetProvider get() {
        return Holder.INSTANCE;
    }

    static void set(MultiblockOffsetProvider provider) {
        Holder.INSTANCE = provider == null ? NONE : provider;
    }

    final class Holder {
        private Holder() {}
        private static MultiblockOffsetProvider INSTANCE = NONE;
    }
}
