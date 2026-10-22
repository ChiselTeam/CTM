package io.github.chiselteam.ctm.client.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.resources.ResourceLocation;

public record CTMModelVariant(ResourceLocation modelLocation, SimpleModelState modelState) {
    public record SimpleModelState(int x, int y, boolean uvLock) {
        public static final SimpleModelState DEFAULT = new SimpleModelState(0, 0, false);
        public static final MapCodec<SimpleModelState> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.INT.optionalFieldOf("x", 0).forGetter(SimpleModelState::x),
                Codec.INT.optionalFieldOf("y", 0).forGetter(SimpleModelState::y),
                Codec.BOOL.optionalFieldOf("uvlock", false).forGetter(SimpleModelState::uvLock)
        ).apply(i, SimpleModelState::new));

        public SimpleModelState {
            if (BlockModelRotation.by(x, y) == null) throw new IllegalArgumentException("Model rotations must be multiples of 90 degrees");
        }

        public CTMModelState asModelState() {
            return CTMModelState.of(BlockModelRotation.by(x, y).getRotation(), uvLock);
        }
    }
}
