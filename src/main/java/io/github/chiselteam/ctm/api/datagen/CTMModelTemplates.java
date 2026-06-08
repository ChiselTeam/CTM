package io.github.chiselteam.ctm.api.datagen;

import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/**
 * Common {@link ModelTemplate}s used by the CTM library for datagen.
 * These templates reference the custom model loaders registered by the CTM library.
 */
public final class CTMModelTemplates {

    private static final Identifier CTM_LOADER = Identifier.fromNamespaceAndPath("ctm", "connected_texture_model");
    private static final Identifier ELDRITCH_LOADER = Identifier.fromNamespaceAndPath("ctm", "eldritch_model");

    /**
     * Standard CTM template: base texture and connected overlay.
     */
    public static final ModelTemplate STANDARD = template(CTM_LOADER, TextureSlot.PARTICLE, CTMTextureSlots.BASE, CTMTextureSlots.OVERLAY_CONNECTED);

    /**
     * TBS CTM template: top/bottom/side textures and connected overlay.
     */
    public static final ModelTemplate TBS = template(CTM_LOADER, TextureSlot.PARTICLE, TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE, CTMTextureSlots.OVERLAY_CONNECTED);

    /**
     * Horizontal CTM template: base texture and horizontal connected overlay.
     */
    public static final ModelTemplate HORIZONTAL = template(CTM_LOADER, TextureSlot.PARTICLE, CTMTextureSlots.BASE, CTMTextureSlots.OVERLAY_HORIZONTAL);

    /**
     * Vertical CTM template: base texture and vertical connected overlay.
     */
    public static final ModelTemplate VERTICAL = template(CTM_LOADER, TextureSlot.PARTICLE, CTMTextureSlots.BASE, CTMTextureSlots.OVERLAY_VERTICAL);

    /**
     * Multiblock CTM template: base texture and multiblock overlay (2x2, 3x3, or 4x4).
     */
    public static final ModelTemplate MULTIBLOCK_2X2 = template(CTM_LOADER, TextureSlot.PARTICLE, CTMTextureSlots.BASE, CTMTextureSlots.OVERLAY_2X2);
    public static final ModelTemplate MULTIBLOCK_3X3 = template(CTM_LOADER, TextureSlot.PARTICLE, CTMTextureSlots.BASE, CTMTextureSlots.OVERLAY_3X3);
    public static final ModelTemplate MULTIBLOCK_4X4 = template(CTM_LOADER, TextureSlot.PARTICLE, CTMTextureSlots.BASE, CTMTextureSlots.OVERLAY_4X4);

    /**
     * Anti-repeat (AR) template: base texture and random rotation overlay.
     */
    public static final ModelTemplate AR = template(CTM_LOADER, TextureSlot.PARTICLE, CTMTextureSlots.BASE, CTMTextureSlots.OVERLAY);

    private static ModelTemplate template(Identifier loader, TextureSlot... requiredSlots) {
        // We use the loader ID as the template "base" model CONCEPTUALLY.
        // In vanilla datagen, ModelTemplate usually takes a parent model ResourceLocation.
        // For custom loaders, the loader ID itself is used in the "loader" field.
        // We pass empty Optional for parent because we want the "loader" field to be the primary differentiator.
        return new ModelTemplate(Optional.empty(), Optional.of(loader.toString()), requiredSlots);
    }

    private CTMModelTemplates() {
    }
}
