package io.github.chiselteam.ctm.impl.model;

import com.google.common.collect.MapMaker;
import io.github.chiselteam.ctm.client.model.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/** Bake-scoped memoization without retaining the model bakery or old resource reloads. */
public final class CTMBakeCache {
    private CTMBakeCache() {}

    private static final ConcurrentMap<Object, WeakReference<Scope>> SCOPES =
            new MapMaker().weakKeys().makeMap();

    public static synchronized Scope scope(CTMBakingContext context) {
        // The atlas sprite getter identifies the bake generation in the 1.21 pipeline.
        Object source = Objects.requireNonNull(context.spriteGetter());
        var reference = SCOPES.get(source);
        Scope scope = reference == null ? null : reference.get();
        if (scope == null) {
            scope = new Scope();
            SCOPES.put(source, new WeakReference<>(scope));
        }
        return scope;
    }

    public static final class Scope {
        private final Map<QuadKey, QuadReference> quads = new HashMap<>();
        private final ReferenceQueue<BakedQuad> queue = new ReferenceQueue<>();

        public synchronized BakedQuad quad(BakedQuad quad) {
            QuadReference expired;
            while ((expired = (QuadReference) queue.poll()) != null)
                quads.remove(expired.key, expired);
            var key = new QuadKey(quad);
            var reference = quads.get(key);
            var shared = reference == null ? null : reference.get();
            if (shared != null) return shared;
            quads.put(key, new QuadReference(key, quad, queue));
            return quad;
        }

        public void finish(CTMBakedModel root) {
            Set<CTMBakedModel> seen = Collections.newSetFromMap(new IdentityHashMap<>());
            var pending = new ArrayDeque<CTMBakedModel>();
            pending.add(Objects.requireNonNull(root));
            while (!pending.isEmpty()) {
                var model = pending.removeFirst();
                if (!seen.add(model) || !(model instanceof CTMQuadStorage storage)) continue;
                storage.visitQuadArrays(array -> {
                    if (array == null) return;
                    for (int i = 0; i < array.length; i++)
                        if (array[i] != null) array[i] = quad(array[i]);
                });
                storage.visitChildModels(child -> { if (child != null) pending.addLast(child); });
            }
        }
    }

    private static final class QuadReference extends WeakReference<BakedQuad> {
        private final QuadKey key;
        QuadReference(QuadKey key, BakedQuad quad, ReferenceQueue<BakedQuad> queue) {
            super(quad, queue);
            this.key = key;
        }
    }

    // BakedQuad has identity equality on 1.21. Keep structural equality, without a strong
    // reference back to the weak value, so unused quads can actually be reclaimed.
    private static final class QuadKey {
        private final int[] vertices;
        private final TextureAtlasSprite sprite;
        private final Direction direction;
        private final int tint;
        private final boolean shade, ambientOcclusion;
        private final int hash;

        QuadKey(BakedQuad quad) {
            vertices = quad.getVertices().clone();
            sprite = quad.getSprite();
            direction = quad.getDirection();
            tint = quad.getTintIndex();
            shade = quad.isShade();
            ambientOcclusion = quad.hasAmbientOcclusion();
            hash = Objects.hash(Arrays.hashCode(vertices), sprite, direction, tint, shade, ambientOcclusion);
        }
        @Override public int hashCode() { return hash; }
        @Override public boolean equals(Object other) {
            return other instanceof QuadKey key && sprite == key.sprite && direction == key.direction
                    && tint == key.tint && shade == key.shade && ambientOcclusion == key.ambientOcclusion
                    && Arrays.equals(vertices, key.vertices);
        }
    }

    public static final class Memo {
        private Entry entry;

        public synchronized CTMBakedModel bake(CTMBakingContext context, Supplier<CTMBakedModel> factory) {
            if (entry != null && entry.matches(context)) return entry.model;
            var scope = scope(context);
            var model = Objects.requireNonNull(factory.get());
            scope.finish(model);
            entry = new Entry(context, scope, model);
            return model;
        }

        private static final class Entry {
            private final WeakReference<Object> baker, sprites, context;
            private final CTMModelState state;
            private final boolean ambientOcclusion;
            @SuppressWarnings("unused") private final Scope scope;
            private final CTMBakedModel model;

            Entry(CTMBakingContext source, Scope scope, CTMBakedModel model) {
                baker = new WeakReference<>(source.baker());
                sprites = new WeakReference<>(source.spriteGetter());
                context = new WeakReference<>(source.context());
                state = source.modelState();
                ambientOcclusion = source.ambientOcclusion();
                this.scope = scope;
                this.model = model;
            }

            boolean matches(CTMBakingContext source) {
                return baker.get() == source.baker() && sprites.get() == source.spriteGetter()
                        && context.get() == source.context() && ambientOcclusion == source.ambientOcclusion()
                        && state.isUvLocked() == source.modelState().isUvLocked()
                        && state.getRotation().equals(source.modelState().getRotation());
            }
        }
    }
}
