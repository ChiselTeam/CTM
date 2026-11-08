package io.github.chiselteam.ctm.impl.model;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class DeferredCTMModel implements DynamicBlockStateModel, CTMQuadStorage {
    private final String family;
    private final Material.Baked particle;
    private volatile BlockStateModel realized;
    private Supplier<BlockStateModel> recipe;
    private Inputs inputs;

    public DeferredCTMModel(String family, Material.Baked particle, Supplier<BlockStateModel> recipe, Inputs inputs) {
        this.family = family;
        this.particle = particle;
        this.recipe = Objects.requireNonNull(recipe);
        this.inputs = inputs;
    }

    private BlockStateModel model() {
        var result = realized;
        if (result == null) synchronized (this) {
            result = realized;
            if (result == null) {
                result = Objects.requireNonNull(recipe.get());
                if (result.materialFlags() != 0 || !Objects.equals(result.particleMaterial(), particle))
                    throw new IllegalStateException("Deferred metadata mismatch: " + family);
                if (inputs != null) {
                    inputs.canonicalize(result);
                    inputs.release();
                }

                realized = result;
                recipe = null;
                inputs = null;
            }
        }
        return result;
    }

    @Override
    public Object createGeometryKey(@NonNull BlockAndTintGetter level, @NonNull BlockPos pos, @NonNull BlockState state, @NonNull RandomSource random) {
        var model = model();
        return model instanceof DynamicBlockStateModel dynamic
                ? dynamic.createGeometryKey(level, pos, state, random) : model;
    }

    @Override
    public void collectParts(@NonNull BlockAndTintGetter level, @NonNull BlockPos pos, @NonNull BlockState state, @NonNull RandomSource random, @NonNull List<BlockStateModelPart> output) {
        var model = model();
        if (model instanceof DynamicBlockStateModel dynamic) dynamic.collectParts(level, pos, state, random, output);
        else model.collectParts(random, output);
    }

    @Override
    public void collectParts(@NonNull RandomSource random, @NonNull List<BlockStateModelPart> output) {
        model().collectParts(random, output);
    }

    @Override
    public Material.@NonNull Baked particleMaterial() {
        return particle;
    }

    @Override
    public int materialFlags() {
        return 0;
    }

    @Override
    public void visitChildModels(Consumer<BlockStateModel> visitor) {
        var model = realized;
        if (model != null) visitor.accept(model);
    }

    public static final class Inputs implements ModelBaker {
        private final Identifier location;
        private final ResolvedModel model;
        private final MaterialBaker materials;
        private final Shared shared;
        private final Set<net.minecraft.client.renderer.texture.TextureAtlasSprite> sprites;
        private boolean released;

        public Inputs(Identifier location, TextureSlots slots, Map<Material, Material.Baked> values, Interner source) {
            this.location = location;
            this.model = new SnapshotModel(location.toString(), slots);
            this.materials = new SnapshotMaterials(values);
            this.shared = generation(source);
            var sprites = new HashSet<net.minecraft.client.renderer.texture.TextureAtlasSprite>();
            for (var material : values.values()) sprites.add(material.sprite());
            this.sprites = Set.copyOf(sprites);
            this.sprites.forEach(shared::register);
        }

        @Override
        public @NonNull ResolvedModel getModel(@NonNull Identifier id) {
            if (!location.equals(id)) throw new IllegalStateException("Unexpected deferred dependency " + id);
            return model;
        }

        @Override
        public @NonNull MaterialBaker materials() {
            return materials;
        }

        @Override
        public @NonNull Interner interner() {
            return shared;
        }

        @Override
        public @NonNull BlockStateModelPart missingBlockModelPart() {
            throw new UnsupportedOperationException("Uncaptured missing model");
        }

        @Override
        public <T> @NonNull T compute(@NonNull SharedOperationKey<T> key) {
            throw new UnsupportedOperationException("Uncaptured bake operation");
        }

        private void canonicalize(BlockStateModel root) {
            var pending = new ArrayDeque<BlockStateModel>();
            var seen = Collections.newSetFromMap(new IdentityHashMap<BlockStateModel, Boolean>());
            pending.add(root);
            while (!pending.isEmpty()) {
                var model = pending.removeFirst();
                if (!seen.add(model) || !(model instanceof CTMQuadStorage storage)) continue;
                storage.visitQuadArrays(array -> {
                    for (int i = 0; i < array.length; i++)
                        if (array[i] != null) {
                            array[i] = shared.quad(array[i]);
                        }
                });
                storage.visitChildModels(pending::add);
            }
        }

        private void release() {
            if (released) throw new IllegalStateException("Inputs already released");
            sprites.forEach(shared::release);
            released = true;
        }
    }

    private record SnapshotModel(String debugName, TextureSlots slots) implements ResolvedModel {
        private static final ContextMap EMPTY_PROPERTIES = new ContextMap.Builder().create(ContextKeySet.EMPTY);

        @Override
        public @NonNull TextureSlots getTopTextureSlots() {
            return slots;
        }

        @Override
        public @NonNull ContextMap getTopAdditionalProperties() {
            return EMPTY_PROPERTIES;
        }

        @Override
        public ResolvedModel parent() {
            return null;
        }

        @Override
        public @NonNull UnbakedModel wrapped() {
            throw new UnsupportedOperationException("No retained unbaked model");
        }
    }

    private static final class SnapshotMaterials extends MaterialBaker {
        private final Map<Material, Material.Baked> values;

        SnapshotMaterials(Map<Material, Material.Baked> values) {
            // Eligible deferred models always capture an explicit particle material.
            super(values.values().iterator().next().sprite());
            this.values = Map.copyOf(values);
        }

        @Override
        protected Material.Baked bake(Material material) {
            return Objects.requireNonNull(values.get(material), "Uncaptured material " + material);
        }

        @Override
        public Material.Baked replacementForMissingMaterial(Material material) {
            return bake(material);
        }
        @Override
        public Material.@NonNull Baked get(@NonNull Material material, ModelDebugName name) {
            return Objects.requireNonNull(values.get(material), "Uncaptured material " + material + " for " + name.debugName());
        }

        @Override
        public Material.@NonNull Baked reportMissingReference(@NonNull String reference, ModelDebugName name) {
            throw new IllegalStateException("Uncaptured missing reference " + reference + " for " + name.debugName());
        }
    }

    private static final Map<ModelBaker.Interner, WeakReference<Shared>> GENERATIONS = new WeakHashMap<>();

    private static synchronized Shared generation(ModelBaker.Interner source) {
        var ref = GENERATIONS.get(source);
        var shared = ref == null ? null : ref.get();
        if (shared == null) {
            shared = new Shared();
            GENERATIONS.put(source, new WeakReference<>(shared));
        }
        return shared;
    }

    private static final class Shared implements ModelBaker.Interner {
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
                if (pool == null || pool.pending < 1) throw new IllegalStateException("Unbalanced sprite ownership");
                return --pool.pending == 0 ? null : pool;
            });
        }

        BakedQuad quad(BakedQuad value) {
            var pool = Objects.requireNonNull(pools.get(value.materialInfo().sprite()), "Unregistered quad material");
            var old = pool.quads().putIfAbsent(value, value);
            return old == null ? value : old;
        }

        public @NonNull Vector3fc vector(@NonNull Vector3fc value) {
            var old = vectors.putIfAbsent(value, value);
            return old == null ? value : old;
        }

        public BakedQuad.@NonNull MaterialInfo materialInfo(BakedQuad.MaterialInfo value) {
            var pool = Objects.requireNonNull(pools.get(value.sprite()), "Unregistered material info");
            var old = pool.materials().putIfAbsent(value, value);
            return old == null ? value : old;
        }

        public @NonNull BakedNormals normals(@NonNull BakedNormals value) {
            var old = normals.putIfAbsent(value, value);
            return old == null ? value : old;
        }

        public @NonNull BakedColors colors(@NonNull BakedColors value) {
            var old = colors.putIfAbsent(value, value);
            return old == null ? value : old;
        }
    }

    private static final class Pool {
        int pending; // Accessed only inside ConcurrentHashMap.compute for this sprite.
        private volatile ConcurrentHashMap<BakedQuad, BakedQuad> quads;
        private volatile ConcurrentHashMap<BakedQuad.MaterialInfo, BakedQuad.MaterialInfo> materials;

        ConcurrentHashMap<BakedQuad, BakedQuad> quads() {
            var result = quads;
            if (result == null) synchronized (this) {
                if ((result = quads) == null) quads = result = new ConcurrentHashMap<>();
            }
            return result;
        }

        ConcurrentHashMap<BakedQuad.MaterialInfo, BakedQuad.MaterialInfo> materials() {
            var result = materials;
            if (result == null) synchronized (this) {
                if ((result = materials) == null) materials = result = new ConcurrentHashMap<>();
            }
            return result;
        }
    }
}
