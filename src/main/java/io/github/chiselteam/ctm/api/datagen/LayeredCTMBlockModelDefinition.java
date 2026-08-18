package io.github.chiselteam.ctm.api.datagen;

import com.mojang.serialization.MapCodec;
import io.github.chiselteam.ctm.client.unbaked.UnbakedLayeredCTMBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.neoforged.neoforge.client.model.block.CustomBlockModelDefinition;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public record LayeredCTMBlockModelDefinition(UnbakedLayeredCTMBlockStateModel model) implements CustomBlockModelDefinition {
    public static final MapCodec<LayeredCTMBlockModelDefinition> CODEC =
            UnbakedLayeredCTMBlockStateModel.CODEC.xmap(LayeredCTMBlockModelDefinition::new,
                    LayeredCTMBlockModelDefinition::model);

    @Override
    public @NonNull Map<BlockState, BlockStateModel.UnbakedRoot> instantiate(
            StateDefinition<Block, BlockState> states, @NonNull Supplier<String> sourceSupplier) {
        Map<BlockState, BlockStateModel.UnbakedRoot> result = new HashMap<>();
        BlockStateModel.UnbakedRoot root = model.asRoot();
        states.getPossibleStates().forEach(state -> result.put(state, root));
        return result;
    }

    @Override
    public @NonNull MapCodec<? extends CustomBlockModelDefinition> codec() {
        return CODEC;
    }
}
