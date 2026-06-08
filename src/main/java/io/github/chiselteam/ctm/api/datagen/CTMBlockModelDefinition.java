package io.github.chiselteam.ctm.api.datagen;

import io.github.chiselteam.ctm.client.unbaked.UnbakedConnectedTextureBlockStateModel;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.neoforged.neoforge.client.model.block.CustomBlockModelDefinition;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A {@link CustomBlockModelDefinition} that wraps an {@link UnbakedConnectedTextureBlockStateModel}.
 * This is used during datagen to produce a blockstate file that uses the CTM model.
 */
public record CTMBlockModelDefinition(UnbakedConnectedTextureBlockStateModel model) implements CustomBlockModelDefinition {

    public static final MapCodec<CTMBlockModelDefinition> CODEC = UnbakedConnectedTextureBlockStateModel.CODEC.xmap(CTMBlockModelDefinition::new, CTMBlockModelDefinition::model);

    @Override
    public @NonNull Map<BlockState, BlockStateModel.UnbakedRoot> instantiate(StateDefinition<Block, BlockState> states, @NonNull Supplier<String> sourceSupplier) {
        Map<BlockState, BlockStateModel.UnbakedRoot> result = new HashMap<>();
        BlockStateModel.UnbakedRoot unbakedRoot = model.asRoot();
        states.getPossibleStates().forEach(state -> result.put(state, unbakedRoot));
        return result;
    }

    @Override
    public @NonNull MapCodec<? extends CustomBlockModelDefinition> codec() {
        return CODEC;
    }
}
