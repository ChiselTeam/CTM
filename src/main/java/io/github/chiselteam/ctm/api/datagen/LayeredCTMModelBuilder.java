package io.github.chiselteam.ctm.api.datagen;

import io.github.chiselteam.ctm.client.unbaked.UnbakedLayeredCTMBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.neoforged.neoforge.client.model.generators.blockstate.CustomBlockStateModelBuilder;
import net.neoforged.neoforge.client.model.generators.blockstate.UnbakedMutator;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class LayeredCTMModelBuilder extends CustomBlockStateModelBuilder {
    private final List<CTMModelBuilder> layers = new ArrayList<>();

    private LayeredCTMModelBuilder() { }

    public static LayeredCTMModelBuilder create() {
        return new LayeredCTMModelBuilder();
    }

    public static LayeredCTMModelBuilder of(CTMModelBuilder bottom, CTMModelBuilder... upperLayers) {
        return create().layer(bottom).layers(upperLayers);
    }

    /** Adds one layer above all layers already present. */
    public LayeredCTMModelBuilder layer(CTMModelBuilder layer) {
        layers.add(layer);
        return this;
    }

    /** Adds layers in bottom-to-top argument order. */
    public LayeredCTMModelBuilder layers(CTMModelBuilder... layers) {
        this.layers.addAll(Arrays.asList(layers));
        return this;
    }

    public List<CTMModelBuilder> layers() {
        return List.copyOf(layers);
    }

    @Override
    public @NonNull LayeredCTMModelBuilder with(@NonNull VariantMutator variantMutator) {
        return this;
    }

    @Override
    public @NonNull LayeredCTMModelBuilder with(@NonNull UnbakedMutator unbakedMutator) {
        LayeredCTMModelBuilder result = create();
        result.layers.addAll(layers);
        return result;
    }

    @Override
    public @NonNull UnbakedLayeredCTMBlockStateModel toUnbaked() {
        if (layers.isEmpty()) {
            throw new IllegalStateException("A layered CTM model requires at least one layer");
        }
        return new UnbakedLayeredCTMBlockStateModel(
                layers.stream().map(CTMModelBuilder::toUnbaked).toList()
        );
    }
}
