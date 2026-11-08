package io.github.chiselteam.ctm.impl.model;

import com.mojang.blaze3d.platform.Transparency;
import com.mojang.math.Quadrant;
import io.github.chiselteam.ctm.api.strategy.CTMLogic;
import io.github.chiselteam.ctm.impl.texture.CTMFaceOrientation;
import io.github.chiselteam.ctm.impl.texture.CTMTextureSet;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class LazyStandardGeometry {
    private static final ModelState IDENTITY = new ModelState() {};
    private static final Map<ModelBaker.Interner, WeakReference<Shared>> GENERATIONS = new WeakHashMap<>();
    private static final Template[][] TEMPLATES = templates();
    private final Material.Baked[] materials = new Material.Baked[CTMLogic.values().length];
    private final int tint, emission, flags;
    private final Direction shadeDirectionOverride;
    private final Shared shared;
    private final Set<TextureAtlasSprite> sprites;

    public LazyStandardGeometry(ModelBaker.Interner source, CTMTextureSet<CTMLogic> textures,
                                int tint, int emission, Direction shadeDirectionOverride) {
        this.tint = tint;
        this.emission = Math.max(0, emission);
        this.shadeDirectionOverride = shadeDirectionOverride;
        synchronized (GENERATIONS) {
            var ref = GENERATIONS.get(source);
            var value = ref == null ? null : ref.get();
            if (value == null) {
                value = new Shared();
                GENERATIONS.put(source, new WeakReference<>(value));
            }
            shared = value;
        }

        for (var logic : CTMLogic.values()) materials[logic.ordinal()] = Objects.requireNonNull(textures.get(logic));
        int f = 0;
        for (var face : TEMPLATES)
            for (var template : face)
                for (var material : materials) {
                    if (transparency(material, template.uvs).hasTranslucent()) f |= BakedQuad.FLAG_TRANSLUCENT;
                    if (material.sprite().contents().isAnimated()) f |= BakedQuad.FLAG_ANIMATED;
                }
        flags = f;
        var unique = new HashSet<TextureAtlasSprite>();
        for (var material : materials) unique.add(material.sprite());
        sprites = Set.copyOf(unique);
        sprites.forEach(shared::register);
    }

    public int flags() {
        return flags;
    }

    public void seedBase(BakedQuad[] quads) {
        for (var q : quads) if (q != null && sprites.contains(q.materialInfo().sprite())) shared.seed(q);
    }

    public Map<Direction, BakedQuad[][]> bake() {
        Map<Direction, BakedQuad[][]> result = new EnumMap<>(Direction.class);
        for (var face : Direction.values()) {
            var corners = new BakedQuad[4][materials.length];
            for (int c = 0; c < 4; c++) {
                var t = TEMPLATES[face.ordinal()][c];
                BakedQuad geometry = null;
                for (int i = 0; i < materials.length; i++) {
                    var info = shared.materialInfo(BakedQuad.MaterialInfo.of(materials[i], transparency(materials[i], t.uvs), tint, shadeDirectionOverride, emission, true));
                    var q = geometry == null ? FaceBakery.bakeQuad(shared, t.from, t.to, t.uvs, Quadrant.R0, info, face, IDENTITY, null, ExtraFaceData.DEFAULT) : retarget(geometry, t.uvs, info);
                    if (geometry == null) geometry = q;
                    corners[c][i] = shared.quad(q);
                }
            }
            result.put(face, corners);
        }
        sprites.forEach(shared::release);
        return result;
    }

    static BakedQuad retarget(BakedQuad geometry, CuboidFace.UVs uv, BakedQuad.MaterialInfo info) {
        return new BakedQuad(geometry.position0(), geometry.position1(), geometry.position2(), geometry.position3(),
                packed(info, uv, 0), packed(info, uv, 1), packed(info, uv, 2), packed(info, uv, 3),
                geometry.direction(), info, geometry.bakedNormals(), geometry.bakedColors());
    }

    private static long packed(BakedQuad.MaterialInfo info, CuboidFace.UVs uv, int vertex) {
        return UVPair.pack(info.sprite().getU(CuboidFace.getU(uv, Quadrant.R0, vertex)), info.sprite().getV(CuboidFace.getV(uv, Quadrant.R0, vertex)));
    }

    private static Transparency transparency(Material.Baked m, CuboidFace.UVs uv) {
        return m.forceTranslucent() ? Transparency.TRANSLUCENT : m.sprite().contents().computeTransparency(
                Math.min(uv.minU(), uv.maxU()) / 16f, Math.min(uv.minV(), uv.maxV()) / 16f,
                Math.max(uv.minU(), uv.maxU()) / 16f, Math.max(uv.minV(), uv.maxV()) / 16f);
    }

    private record Template(Vector3fc from, Vector3fc to, CuboidFace.UVs uvs) {
    }

    private static Template[][] templates() {
        var result = new Template[6][4];
        for (var face : Direction.values()) {
            var directions = CTMFaceOrientation.localDirections(face);
            for (int c = 0; c < 4; c++) {
                var corner = face.getUnitVec3i().offset(directions[c].getUnitVec3i()).offset(directions[(c + 1) % 4].getUnitVec3i()).offset(1, 1, 1).multiply(8);
                var from = new Vector3f(Math.min(8, corner.getX()), Math.min(8, corner.getY()), Math.min(8, corner.getZ()));
                var to = new Vector3f(Math.max(8, corner.getX()), Math.max(8, corner.getY()), Math.max(8, corner.getZ()));
                result[face.ordinal()][c] = new Template(from, to, FaceBakery.defaultFaceUV(from, to, face));
            }
        }
        return result;
    }

    static final class Shared implements ModelBaker.Interner {
        final ConcurrentHashMap<TextureAtlasSprite, Pool> pools = new ConcurrentHashMap<>();
        final ConcurrentHashMap<Vector3fc, Vector3fc> vectors = new ConcurrentHashMap<>();
        final ConcurrentHashMap<BakedNormals, BakedNormals> normals = new ConcurrentHashMap<>();
        final ConcurrentHashMap<BakedColors, BakedColors> colors = new ConcurrentHashMap<>();

        void register(TextureAtlasSprite sprite) {
            pools.compute(sprite, (_, pool) -> {
                if (pool == null) pool = new Pool();
                pool.pending++;
                return pool;
            });
        }

        void release(TextureAtlasSprite sprite) {
            pools.compute(sprite, (_, pool) -> {
                if (pool == null || pool.pending < 1) throw new IllegalStateException("Unbalanced Standard sprite ownership");
                return --pool.pending == 0 ? null : pool;
            });
        }

        void seed(BakedQuad value) {
            var pool = pools.get(value.materialInfo().sprite());
            if (pool != null) pool.quads().putIfAbsent(value, value);
        }

        BakedQuad quad(BakedQuad value) {
            var pool = Objects.requireNonNull(pools.get(value.materialInfo().sprite()), "Unregistered Standard quad");
            var old = pool.quads().putIfAbsent(value, value);
            return old == null ? value : old;
        }

        public @NonNull Vector3fc vector(@NonNull Vector3fc v) {
            var old = vectors.putIfAbsent(v, v);
            return old == null ? v : old;
        }

        public BakedQuad.@NonNull MaterialInfo materialInfo(BakedQuad.MaterialInfo m) {
            var pool = Objects.requireNonNull(pools.get(m.sprite()), "Unregistered Standard material");
            var old = pool.materials().putIfAbsent(m, m);
            return old == null ? m : old;
        }

        public @NonNull BakedNormals normals(@NonNull BakedNormals n) {
            var old = normals.putIfAbsent(n, n);
            return old == null ? n : old;
        }

        public @NonNull BakedColors colors(@NonNull BakedColors c) {
            var old = colors.putIfAbsent(c, c);
            return old == null ? c : old;
        }
    }

    static final class Pool {
        int pending;
        private volatile ConcurrentHashMap<BakedQuad, BakedQuad> quads;
        private volatile ConcurrentHashMap<BakedQuad.MaterialInfo, BakedQuad.MaterialInfo> materials;

        ConcurrentHashMap<BakedQuad, BakedQuad> quads() {
            var value = quads;
            if (value == null) synchronized (this) {
                if ((value = quads) == null) quads = value = new ConcurrentHashMap<>();
            }
            return value;
        }

        ConcurrentHashMap<BakedQuad.MaterialInfo, BakedQuad.MaterialInfo> materials() {
            var value = materials;
            if (value == null) synchronized (this) {
                if ((value = materials) == null) materials = value = new ConcurrentHashMap<>();
            }
            return value;
        }
    }
}
