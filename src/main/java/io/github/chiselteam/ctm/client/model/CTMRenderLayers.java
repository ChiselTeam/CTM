package io.github.chiselteam.ctm.client.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;

public final class CTMRenderLayers {
    private static final LoadingCache<SpriteContents, Transparency> CACHE = CacheBuilder.newBuilder().weakKeys().build(CacheLoader.from(CTMRenderLayers::classify));

    private CTMRenderLayers() {
    }

    public static RenderType renderType(BakedQuad quad) {
        return transparency(quad.getSprite()).renderType();
    }

    public static Transparency transparency(TextureAtlasSprite sprite) {
        var contents = sprite.contents();
        return contents == null ? Transparency.OPAQUE : CACHE.getUnchecked(contents);
    }

    private static Transparency classify(SpriteContents contents) {
        var image = contents.getOriginalImage();
        boolean cutout = false;
        // Scan original texels across all frames, rather than mipmaps whose filtering introduces partial alpha.
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int alpha = image.getPixelRGBA(x, y) >>> 24;
                if (alpha > 0 && alpha < 255) return Transparency.TRANSLUCENT;
                if (alpha == 0) cutout = true;
            }
        }
        if (cutout && contents.metadata().getSection(AnimationMetadataSection.SERIALIZER)
                .map(AnimationMetadataSection::isInterpolatedFrames).orElse(false)) {
            return Transparency.TRANSLUCENT;
        }
        return cutout ? Transparency.CUTOUT : Transparency.OPAQUE;
    }

    public enum Transparency {
        OPAQUE, CUTOUT, TRANSLUCENT;

        public RenderType renderType() {
            return switch (this) {
                case OPAQUE -> RenderType.solid();
                case CUTOUT -> RenderType.cutoutMipped();
                case TRANSLUCENT -> RenderType.translucent();
            };
        }
    }
}
