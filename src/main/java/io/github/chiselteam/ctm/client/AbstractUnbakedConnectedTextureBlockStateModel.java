package io.github.chiselteam.ctm.client;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import io.github.chiselteam.ctm.api.model.CTMOverlayRule;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.api.strategy.CTMLogic;
import io.github.chiselteam.ctm.client.model.*;
import io.github.chiselteam.ctm.client.model.CTMFace;
import io.github.chiselteam.ctm.client.unbaked.CTMModelCodecs;
import io.github.chiselteam.ctm.impl.texture.CTMTextureSet;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.model.SimpleModelState;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.*;

public abstract class AbstractUnbakedConnectedTextureBlockStateModel implements CTMUnbakedModel {
    protected BlockModel model;
    protected CTMModelState state;
    protected Transformation rootTransform;

    protected final ResourceLocation modelLocation;
    protected final Pair<Vector3f, Vector3f> element;
    protected final Set<Direction> connectedFaces;
    protected final boolean renderOverlayOnAllFaces;
    protected final CTMVariant variant;
    protected final int baseTintIndex;
    protected final int baseEmissivity;
    protected final int tintIndex;
    protected final int emissivity;
    protected final boolean shade;
    protected final boolean ambientOcclusion;
    protected final boolean eldritch;
    protected final CTMBlockPredicate connectionPredicate;
    protected final List<CTMModelCodecs.UnbakedOverlayRule> overlays;
    protected final Map<String, ResourceLocation> textureSlots;
    protected final CTMModelVariant.SimpleModelState modelState;

    protected AbstractUnbakedConnectedTextureBlockStateModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, ResourceLocation> textureSlots, CTMModelVariant.SimpleModelState modelState) {
        this.modelLocation = modelLocation;
        this.element = element;
        EnumSet<Direction> copiedConnectedFaces = EnumSet.noneOf(Direction.class);
        copiedConnectedFaces.addAll(connectedFaces);
        this.connectedFaces = Collections.unmodifiableSet(copiedConnectedFaces);
        this.renderOverlayOnAllFaces = renderOverlayOnAllFaces;
        this.variant = variant;
        this.baseTintIndex = baseTintIndex;
        this.baseEmissivity = baseEmissivity;
        this.tintIndex = tintIndex;
        this.emissivity = emissivity;
        this.shade = shade;
        this.ambientOcclusion = ambientOcclusion;
        this.eldritch = eldritch;
        this.connectionPredicate = connectionPredicate;
        this.overlays = List.copyOf(overlays);
        this.textureSlots = Map.copyOf(textureSlots);
        this.modelState = modelState;
    }

    protected AbstractUnbakedConnectedTextureBlockStateModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, ResourceLocation> textureSlots) {
        this(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots, CTMModelVariant.SimpleModelState.DEFAULT);
    }

    protected AbstractUnbakedConnectedTextureBlockStateModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, ResourceLocation> textureSlots) {
        this(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, true, eldritch, connectionPredicate, overlays, textureSlots);
    }

    protected AbstractUnbakedConnectedTextureBlockStateModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays) {
        this(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, true, eldritch, connectionPredicate, overlays, Map.of());
    }

    protected AbstractUnbakedConnectedTextureBlockStateModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean eldritch) {
        this(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, true, eldritch, CTMBlockPredicate.sameBlock(), List.of());
    }

    protected AbstractUnbakedConnectedTextureBlockStateModel(ResourceLocation modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity) {
        this(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, true, false, CTMBlockPredicate.sameBlock(), List.of());
    }

    @Override
    public void resolveDependencies(CTMUnbakedModel.Resolver resolver) {
        resolver.markDependency(modelLocation);
    }

    public abstract @NotNull CTMBakedModel bake(@NotNull CTMBakingContext baker);

    protected void prepareBakery(CTMBakingContext baker) {
        model = baker.getModel(modelLocation);
        state = CTMModelState.of(new SimpleModelState(baker.modelState().getRotation().compose(modelState.asModelState().getRotation()), baker.modelState().isUvLocked() || modelState.uvLock()));
        rootTransform = model.customData.getRootTransform().compose(baker.context().getRootTransform());
        if (!rootTransform.isIdentity())
            state = CTMModelState.of(UnbakedGeometryHelper.composeRootTransformIntoModelState(state, rootTransform));
    }

    protected List<CTMOverlayRule> bakeOverlays() {
        List<CTMOverlayRule> baked = new ArrayList<>();
        for (CTMModelCodecs.UnbakedOverlayRule unbaked : overlays) {
            Material mat = getMaterial(unbaked.material());
            if (mat != null) {
                baked.add(new CTMOverlayRule(mat, unbaked.faces(), unbaked.condition(), unbaked.priority(), unbaked.tintIndex(), unbaked.emissivity()));
            }
        }
        return baked;
    }

    protected Material getMaterial(String name) {
        ResourceLocation override = textureSlots.get(name);
        if (override != null) {
            return new Material(InventoryMenu.BLOCK_ATLAS, override);
        }
        Material mat = model.hasTexture(name) ? model.getMaterial(name) : null;
        if (mat == null) {
            if (name.equals("base_texture")) {
                mat = model.hasTexture("all") ? model.getMaterial("all") : null;
                if (mat == null) {
                    mat = model.hasTexture("layer0") ? model.getMaterial("layer0") : null;
                }
            }
        }
        return mat;
    }

    protected CTMTextureSet<CTMLogic> bakeStandardTextureSet(CTMBakingContext baker) {
        var textures = new CTMTextureSet<>(CTMLogic.class);
        for (var logic : CTMLogic.values()) {
            textures.put(logic, bakeMaterial(baker, getMaterial(logic.getStandardTextureSlot())));
        }
        return textures;
    }

    protected BakedTexture bakeMaterial(CTMBakingContext baker, Material material) {
        if (material == null) return null;
        var sprite = baker.spriteGetter().apply(material);
        CTMRenderLayers.transparency(sprite);
        return new BakedTexture(sprite, false);
    }

    protected Map<CTMOverlayRule, Map<Direction, BakedQuad>> bakeOverlayQuads(CTMBakingContext baker, List<CTMOverlayRule> rules, Vector3f from, Vector3f to, CTMModelState state) {
        Map<CTMOverlayRule, Map<Direction, BakedQuad>> ruleQuads = new HashMap<>();
        for (var rule : rules) {
            Map<Direction, BakedQuad> quads = new EnumMap<>(Direction.class);
            var bakedMat = bakeMaterial(baker, rule.material());
            if (bakedMat != null) {
                for (var face : rule.faces()) {
                    var cull = getCullface(face, from, to);
                    var overlayFace = new CTMFace(cull, rule.tintIndex(), "", new CTMFace.UVs(0, 0, 16, 16), CTMFace.Quadrant.R0);
                    var offsets = getOffsets(face, from, to);
                    quads.put(face, CTMFaceBakery.bakeQuad(baker, offsets[0], offsets[1], overlayFace, bakedMat, face, state, null, shade, rule.emissivity()));
                }
            }
            ruleQuads.put(rule, quads);
        }
        return ruleQuads;
    }

    protected <T> Map<Direction, T> remapQuadDirections(Map<Direction, T> quads) {
        if (state.transformation().isIdentity()) return quads;
        Map<Direction, T> remapped = new EnumMap<>(Direction.class);
        quads.forEach((source, value) -> remapped.put(bakedDirection(source, value), value));
        return remapped;
    }

    protected Set<Direction> remapDirections(Set<Direction> directions, Map<Direction, ? extends Object> bakedFaces) {
        if (state.transformation().isIdentity()) return directions;
        Set<Direction> remapped = EnumSet.noneOf(Direction.class);
        for (var direction : directions) {
            var value = bakedFaces.get(direction);
            remapped.add(bakedDirection(direction, value));
        }
        return remapped;
    }

    protected Map<CTMOverlayRule, Map<Direction, BakedQuad>> remapRuleQuadDirections(Map<CTMOverlayRule, Map<Direction, BakedQuad>> ruleQuads) {
        ruleQuads.replaceAll((ignored, quads) -> remapQuadDirections(quads));
        return ruleQuads;
    }

    private Direction bakedDirection(Direction fallback, Object value) {
        var quad = firstQuad(value);
        return quad != null ? quad.getDirection() : Direction.rotate(state.transformation().getMatrix(), fallback);
    }

    private static BakedQuad firstQuad(Object value) {
        if (value instanceof BakedQuad quad) return quad;
        if (value instanceof Object[] array) {
            for (Object entry : array) {
                var quad = firstQuad(entry);
                if (quad != null) return quad;
            }
        }
        return null;
    }

    protected Direction getCullface(Direction direction, Vector3f from, Vector3f to) {
        boolean cull = switch (direction) {
            case DOWN -> from.y() == 0.0F;
            case UP -> to.y() == 16.0F;
            case NORTH -> from.z() == 0.0F;
            case SOUTH -> to.z() == 16.0F;
            case WEST -> from.x() == 0.0F;
            case EAST -> to.x() == 16.0F;
        };
        return cull ? direction : null;
    }

    protected CTMFace.UVs getRelativeUVs(Direction face, Vector3f from, Vector3f to) {
        float u0, v0, u1, v1;
        switch (face) {
            case UP -> {
                u0 = from.x();
                v0 = from.z();
                u1 = to.x();
                v1 = to.z();
            }
            case DOWN -> {
                u0 = from.x();
                v0 = 16 - to.z();
                u1 = to.x();
                v1 = 16 - from.z();
            }
            case NORTH -> {
                u0 = 16 - to.x();
                v0 = 16 - to.y();
                u1 = 16 - from.x();
                v1 = 16 - from.y();
            }
            case SOUTH -> {
                u0 = from.x();
                v0 = 16 - to.y();
                u1 = to.x();
                v1 = 16 - from.y();
            }
            case WEST -> {
                u0 = from.z();
                v0 = 16 - to.y();
                u1 = to.z();
                v1 = 16 - from.y();
            }
            case EAST -> {
                u0 = 16 - to.z();
                v0 = 16 - to.y();
                u1 = 16 - from.z();
                v1 = 16 - from.y();
            }
            default -> {
                u0 = 0;
                v0 = 0;
                u1 = 16;
                v1 = 16;
            }
        }
        return new CTMFace.UVs(Math.clamp(u0, 0, 16), Math.clamp(v0, 0, 16), Math.clamp(u1, 0, 16), Math.clamp(v1, 0, 16));
    }

    protected Vector3f[] getOffsets(Direction face, Vector3f from, Vector3f to) {
        float offset = 0.01f;
        Vector3f[] offsets = new Vector3f[] {
                new Vector3f(from), new Vector3f(to)
        };

        if (variant.waterOffset()) {
            switch (face) {
                case DOWN -> {
                    offsets[0].y -= offset;
                    offsets[1].y -= offset;
                }
                case UP -> {
                    offsets[0].y += offset;
                    offsets[1].y += offset;
                }
                case NORTH -> {
                    offsets[0].z -= offset;
                    offsets[1].z -= offset;
                }
                case SOUTH -> {
                    offsets[0].z += offset;
                    offsets[1].z += offset;
                }
                case WEST -> {
                    offsets[0].x -= offset;
                    offsets[1].x -= offset;
                }
                case EAST -> {
                    offsets[0].x += offset;
                    offsets[1].x += offset;
                }
            }
        }
        return offsets;
    }
}
