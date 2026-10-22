package io.github.chiselteam.ctm.api.datagen;

import io.github.chiselteam.ctm.client.unbaked.UnbakedLayeredCTMBlockStateModel;
import io.github.chiselteam.ctm.api.datagen.VariantMutator;
import io.github.chiselteam.ctm.api.datagen.UnbakedMutator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class LayeredCTMModelBuilder {
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

    public @NotNull LayeredCTMModelBuilder with(@NotNull VariantMutator variantMutator) {
        LayeredCTMModelBuilder result = create();
        layers.stream().map(layer -> layer.with(variantMutator)).forEach(result.layers::add);
        return result;
    }

    public @NotNull LayeredCTMModelBuilder with(@NotNull UnbakedMutator unbakedMutator) {
        LayeredCTMModelBuilder result = create();
        layers.stream().map(layer -> layer.with(unbakedMutator)).forEach(result.layers::add);
        return result;
    }

    public @NotNull UnbakedLayeredCTMBlockStateModel toUnbaked() {
        if (layers.isEmpty()) {
            throw new IllegalStateException("A layered CTM model requires at least one layer");
        }
        return new UnbakedLayeredCTMBlockStateModel(
                layers.stream().map(CTMModelBuilder::toUnbaked).toList()
        );
    }
}
