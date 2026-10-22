package io.github.chiselteam.ctm.client.model;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;

import java.util.function.Function;

public record CTMBakingContext(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, CTMModelState modelState, IGeometryBakingContext context, boolean ambientOcclusion) {
    public BlockModel getModel(ResourceLocation location) {
        if (baker.getModel(location) instanceof BlockModel model) return model;
        throw new IllegalArgumentException("CTM texture source must be a block model: " + location);
    }

    public CTMBakingContext withAmbientOcclusion(boolean enabled) {
        return new CTMBakingContext(baker, spriteGetter, modelState, context, enabled);
    }
}
