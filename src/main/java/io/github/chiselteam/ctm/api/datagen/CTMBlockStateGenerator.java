package io.github.chiselteam.ctm.api.datagen;

import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;

/**
 * A {@link BlockModelDefinitionGenerator} that dispatches to a CTM model.
 * This class is used in vanilla {@code BlockModelGenerators} to register CTM blockstates.
 */
public class CTMBlockStateGenerator implements BlockModelDefinitionGenerator {

    private final Block block;
    private final CTMModelBuilder builder;

    private CTMBlockStateGenerator(Block block, CTMModelBuilder builder) {
        this.block = block;
        this.builder = builder;
    }

    /**
     * Creates a generator for the given block using the provided CTM builder.
     */
    public static CTMBlockStateGenerator of(Block block, CTMModelBuilder builder) {
        return new CTMBlockStateGenerator(block, builder);
    }

    @Override
    public @NonNull Block block() {
        return block;
    }

    @Override
    public @NonNull BlockStateModelDispatcher create() {
        // CTM library's model loader is registered via RegisterBlockStateModels.
        // The builder produces an UnbakedConnectedTextureBlockStateModel.
        // We use it as the unbaked root for all states of this block.
        return new BlockStateModelDispatcher(new CTMBlockModelDefinition(builder.toUnbaked()));
    }
}
