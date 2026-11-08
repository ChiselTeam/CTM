package io.github.chiselteam.ctm.impl.model;

import com.mojang.math.Transformation;
import io.github.chiselteam.ctm.api.strategy.CTMLogic;
import io.github.chiselteam.ctm.client.model.*;
import io.github.chiselteam.ctm.impl.texture.CTMFaceOrientation;
import io.github.chiselteam.ctm.impl.texture.CTMTextureSet;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/** Captured full-cube Standard geometry; no bakery, resource manager, or model parents. */
public final class LazyStandardGeometry {
    private static final CTMModelState IDENTITY = CTMModelState.of(Transformation.identity(), false);
    private final BakedTexture[] materials = new BakedTexture[CTMLogic.values().length];
    private final int tint, emission;
    private final boolean shade, ambientOcclusion;
    private final CTMBakeCache.Scope scope;

    public LazyStandardGeometry(CTMBakeCache.Scope scope, CTMTextureSet<CTMLogic> textures,
                                int tint, int emission, boolean shade, boolean ambientOcclusion) {
        this.scope = scope;
        this.tint = tint;
        this.emission = emission;
        this.shade = shade;
        this.ambientOcclusion = ambientOcclusion;
        for (var logic : CTMLogic.values()) materials[logic.ordinal()] = Objects.requireNonNull(textures.get(logic));
    }

    public void seedBase(BakedQuad[] quads) {
        if (quads == null) return;
        for (int i = 0; i < quads.length; i++) if (quads[i] != null) quads[i] = scope.quad(quads[i]);
    }

    public Map<Direction, BakedQuad[][]> bake() {
        var context = new CTMBakingContext(null, null, IDENTITY, null, ambientOcclusion);
        Map<Direction, BakedQuad[][]> result = new EnumMap<>(Direction.class);
        for (var face : Direction.values()) {
            var corners = new BakedQuad[4][materials.length];
            var directions = CTMFaceOrientation.localDirections(face);
            for (int c = 0; c < 4; c++) {
                var corner = face.getNormal().offset(directions[c].getNormal())
                        .offset(directions[(c + 1) % 4].getNormal()).offset(1, 1, 1).multiply(8);
                var from = new Vector3f(Math.min(8, corner.getX()), Math.min(8, corner.getY()), Math.min(8, corner.getZ()));
                var to = new Vector3f(Math.max(8, corner.getX()), Math.max(8, corner.getY()), Math.max(8, corner.getZ()));
                var uvs = switch (face) {
                    case UP -> new CTMFace.UVs(from.x(), from.z(), to.x(), to.z());
                    case DOWN -> new CTMFace.UVs(from.x(), 16 - to.z(), to.x(), 16 - from.z());
                    case NORTH -> new CTMFace.UVs(16 - to.x(), 16 - to.y(), 16 - from.x(), 16 - from.y());
                    case SOUTH -> new CTMFace.UVs(from.x(), 16 - to.y(), to.x(), 16 - from.y());
                    case WEST -> new CTMFace.UVs(from.z(), 16 - to.y(), to.z(), 16 - from.y());
                    case EAST -> new CTMFace.UVs(16 - to.z(), 16 - to.y(), 16 - from.z(), 16 - from.y());
                };
                var definition = new CTMFace(face, tint, "", uvs, CTMFace.Quadrant.R0);
                for (int i = 0; i < materials.length; i++)
                    corners[c][i] = scope.quad(CTMFaceBakery.bakeQuad(context, from, to, definition,
                            materials[i], face, IDENTITY, null, shade, emission));
            }
            result.put(face, corners);
        }
        return result;
    }
}
