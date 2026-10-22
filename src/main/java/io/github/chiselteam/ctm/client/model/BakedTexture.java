package io.github.chiselteam.ctm.client.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public record BakedTexture(TextureAtlasSprite sprite, boolean diffuse) {
}
