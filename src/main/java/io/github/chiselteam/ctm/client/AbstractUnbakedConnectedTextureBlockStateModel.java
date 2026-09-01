package io.github.chiselteam.ctm.client;

import com.mojang.datafixers.util.Pair;
import io.github.chiselteam.ctm.api.model.CTMOverlayRule;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.client.unbaked.CTMModelCodecs;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.*;

public abstract class AbstractUnbakedConnectedTextureBlockStateModel implements CustomUnbakedBlockStateModel, UnbakedModel {

    protected final Identifier modelLocation;
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
    protected final Map<String, Identifier> textureSlots;
    protected final Variant.SimpleModelState modelState;

    protected AbstractUnbakedConnectedTextureBlockStateModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots, Variant.SimpleModelState modelState) {
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

    protected AbstractUnbakedConnectedTextureBlockStateModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean ambientOcclusion, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots) {
        this(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, ambientOcclusion, eldritch, connectionPredicate, overlays, textureSlots, Variant.SimpleModelState.DEFAULT);
    }

    protected AbstractUnbakedConnectedTextureBlockStateModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays, Map<String, Identifier> textureSlots) {
        this(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, true, eldritch, connectionPredicate, overlays, textureSlots);
    }

    protected AbstractUnbakedConnectedTextureBlockStateModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean shade, boolean eldritch, CTMBlockPredicate connectionPredicate, List<CTMModelCodecs.UnbakedOverlayRule> overlays) {
        this(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, shade, true, eldritch, connectionPredicate, overlays, Map.of());
    }

    protected AbstractUnbakedConnectedTextureBlockStateModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity, boolean eldritch) {
        this(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, true, eldritch, CTMBlockPredicate.sameBlock(), List.of());
    }

    protected AbstractUnbakedConnectedTextureBlockStateModel(Identifier modelLocation, Pair<Vector3f, Vector3f> element, Set<Direction> connectedFaces, boolean renderOverlayOnAllFaces, CTMVariant variant, int baseTintIndex, int baseEmissivity, int tintIndex, int emissivity) {
        this(modelLocation, element, connectedFaces, renderOverlayOnAllFaces, variant, baseTintIndex, baseEmissivity, tintIndex, emissivity, true, false, CTMBlockPredicate.sameBlock(), List.of());
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        resolver.markDependency(modelLocation);
    }

    public abstract @NonNull BlockStateModel bake(@NonNull ModelBaker baker);

    protected List<CTMOverlayRule> bakeOverlays(ResolvedModel model) {
        List<CTMOverlayRule> baked = new ArrayList<>();
        for (CTMModelCodecs.UnbakedOverlayRule unbaked : overlays) {
            Material mat = getMaterial(model, unbaked.material());
            if (mat != null) {
                baked.add(new CTMOverlayRule(mat, unbaked.faces(), unbaked.condition(), unbaked.priority(), unbaked.tintIndex(), unbaked.emissivity()));
            }
        }
        return baked;
    }

    protected Material getMaterial(ResolvedModel model, String name) {
        Identifier override = textureSlots.get(name);
        if (override != null) {
            return new Material(override, false);
        }
        Material mat = model.getTopTextureSlots().getMaterial(name);
        if (mat == null) {
            if (name.equals("base_texture") || name.equals("overlay_texture") || name.equals("overlay_connected")) {
                mat = model.getTopTextureSlots().getMaterial("all");
                if (mat == null) {
                    mat = model.getTopTextureSlots().getMaterial("layer0");
                }
            }
        }
        return mat;
    }

    protected Material.Baked bakeMaterial(ModelBaker baker, Material material, ResolvedModel context) {
        if (material == null) return null;
        return baker.materials().get(material, context);
    }

    protected Map<CTMOverlayRule, Map<Direction, BakedQuad>> bakeOverlayQuads(ModelBaker baker, List<CTMOverlayRule> rules, ResolvedModel model, Vector3f from, Vector3f to, ModelState state) {
        Map<CTMOverlayRule, Map<Direction, BakedQuad>> ruleQuads = new HashMap<>();
        for (CTMOverlayRule rule : rules) {
            Map<Direction, BakedQuad> quads = new EnumMap<>(Direction.class);
            Material.Baked bakedMat = bakeMaterial(baker, rule.material(), model);
            if (bakedMat != null) {
                for (Direction face : rule.faces()) {
                    Direction cull = getCullface(face, from, to);
                    CuboidFace overlayFace = new CuboidFace(cull, rule.tintIndex(), "", new CuboidFace.UVs(0, 0, 16, 16), com.mojang.math.Quadrant.R0);
                    Vector3f[] offsets = getOffsets(face, from, to);
                    quads.put(face, FaceBakery.bakeQuad(baker, offsets[0], offsets[1], overlayFace, bakedMat, face, state, null, shade, rule.emissivity()));
                }
            }
            ruleQuads.put(rule, quads);
        }
        return ruleQuads;
    }

    /** Rekeys face-indexed data to the direction produced by the baked model transformation. */
    protected <T> Map<Direction, T> remapQuadDirections(Map<Direction, T> quads) {
        if (modelState.equals(Variant.SimpleModelState.DEFAULT)) return quads;
        Map<Direction, T> remapped = new EnumMap<>(Direction.class);
        quads.forEach((source, value) -> remapped.put(bakedDirection(source, value), value));
        return remapped;
    }

    protected Set<Direction> remapDirections(Set<Direction> directions, Map<Direction, ? extends Object> bakedFaces) {
        if (modelState.equals(Variant.SimpleModelState.DEFAULT)) return directions;
        Set<Direction> remapped = EnumSet.noneOf(Direction.class);
        for (Direction direction : directions) {
            Object value = bakedFaces.get(direction);
            remapped.add(bakedDirection(direction, value));
        }
        return remapped;
    }

    protected Map<CTMOverlayRule, Map<Direction, BakedQuad>> remapRuleQuadDirections(Map<CTMOverlayRule, Map<Direction, BakedQuad>> ruleQuads) {
        ruleQuads.replaceAll((_, quads) -> remapQuadDirections(quads));
        return ruleQuads;
    }

    private Direction bakedDirection(Direction fallback, Object value) {
        BakedQuad quad = value instanceof BakedQuad q ? q
                : value instanceof BakedQuad[] array ? Arrays.stream(array).filter(Objects::nonNull).findFirst().orElse(null)
                : null;
        return quad != null && quad.direction() != null ? quad.direction() : fallback;
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

    protected CuboidFace.UVs getRelativeUVs(Direction face, Vector3f from, Vector3f to) {
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
        return new CuboidFace.UVs(Math.clamp(u0, 0, 16), Math.clamp(v0, 0, 16), Math.clamp(u1, 0, 16), Math.clamp(v1, 0, 16));
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
