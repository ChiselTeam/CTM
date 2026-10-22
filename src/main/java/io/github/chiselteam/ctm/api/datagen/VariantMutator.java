package io.github.chiselteam.ctm.api.datagen;

import io.github.chiselteam.ctm.client.model.CTMModelVariant;

@FunctionalInterface
public interface VariantMutator {
    CTMModelVariant apply(CTMModelVariant variant);
}
