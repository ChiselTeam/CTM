package io.github.chiselteam.ctm.api.datagen;

import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.block.CustomBlockModelDefinition;
import org.jspecify.annotations.NonNull;

/**
 * A {@link BlockModelDefinitionGenerator} that dispatches to a CTM model.
 * This class is used in vanilla {@code BlockModelGenerators} to register CTM blockstates.
 */
public class CTMBlockStateGenerator implements BlockModelDefinitionGenerator {

    private final Block block;
    private final CustomBlockModelDefinition definition;

    private CTMBlockStateGenerator(Block block, CustomBlockModelDefinition definition) {
        this.block = block;
        this.definition = definition;
    }

    /**
     * Creates a generator for the given block using the provided CTM builder.
     */
    public static CTMBlockStateGenerator of(Block block, CTMModelBuilder builder) {
        return new CTMBlockStateGenerator(block, new CTMBlockModelDefinition(builder.toUnbaked()));
    }

    /** Creates a generator for independently connecting CTM layers. */
    public static CTMBlockStateGenerator of(Block block, LayeredCTMModelBuilder builder) {
        return new CTMBlockStateGenerator(block, new LayeredCTMBlockModelDefinition(builder.toUnbaked()));
    }

    /** Named alias for {@link #of(Block, LayeredCTMModelBuilder)}. */
    public static CTMBlockStateGenerator layered(Block block, LayeredCTMModelBuilder builder) {
        return of(block, builder);
    }

    @Override
    public @NonNull Block block() {
        return block;
    }

    @Override
    public @NonNull BlockStateModelDispatcher create() {
        // CTM library's model loader is registered via RegisterBlockStateModels.
        // Use the definition as the unbaked root for every state of this block.
        return new BlockStateModelDispatcher(definition);
    }
}
