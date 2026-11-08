package io.github.chiselteam.ctm.impl.model;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Supplier;

public final class CTMBakeCache {
    private CTMBakeCache() {}

    private static final ReferenceQueue<ModelBaker.Interner> QUEUE = new ReferenceQueue<>();
    private static final Map<Key, WeakReference<Scope>> SCOPES = new HashMap<>();

    private static final class Key extends WeakReference<ModelBaker.Interner> {
        private final int hash;
        Key(ModelBaker.Interner source) { super(source, QUEUE); hash = System.identityHashCode(source); }
        @Override public int hashCode() { return hash; }
        @Override public boolean equals(Object other) {
            return this == other || other instanceof Key key && get() != null && get() == key.get();
        }
    }

    private static synchronized Scope scope(ModelBaker baker) {
        Key expired;
        while ((expired = (Key) QUEUE.poll()) != null) SCOPES.remove(expired);
        var key = new Key(Objects.requireNonNull(baker.interner()));
        var reference = SCOPES.get(key);
        Scope scope = reference == null ? null : reference.get();
        if (scope == null) {
            scope = new Scope();
            SCOPES.put(key, new WeakReference<>(scope));
        }
        return scope;
    }

    private static final class Scope {
        private final Interner<BakedQuad> quads = Interners.newWeakInterner();
        void finish(BlockStateModel root) {
            Set<BlockStateModel> seen = Collections.newSetFromMap(new IdentityHashMap<>());
            var pending = new ArrayDeque<BlockStateModel>();
            pending.add(Objects.requireNonNull(root));
            while (!pending.isEmpty()) {
                var model = pending.removeFirst();
                if (!seen.add(model) || !(model instanceof CTMQuadStorage storage)) continue;
                storage.visitQuadArrays(array -> {
                    if (array == null) return;
                    for (int i = 0; i < array.length; i++) {
                        var quad = array[i];
                        if (quad != null) {
                            var shared = quads.intern(quad);
                            if (shared != quad) array[i] = shared;
                        }
                    }
                });
                storage.visitChildModels(child -> { if (child != null) pending.addLast(child); });
            }
        }
    }

    public static final class Memo {
        private volatile Entry entry;
        private record Entry(WeakReference<ModelBaker> baker, Scope scope, BlockStateModel model) {}
        public BlockStateModel bake(ModelBaker baker, Supplier<BlockStateModel> factory) {
            return bake(baker, factory, true);
        }
        private BlockStateModel bake(ModelBaker baker, Supplier<BlockStateModel> factory, boolean finish) {
            var current = entry;
            if (current != null && current.baker.get() == baker) return current.model;
            synchronized (this) {
                current = entry;
                if (current != null && current.baker.get() == baker) return current.model;
                var scope = scope(baker);
                var model = Objects.requireNonNull(factory.get());
                if (finish) scope.finish(model);
                entry = new Entry(new WeakReference<>(baker), scope, model);
                return model;
            }
        }
    }


    public static BlockStateModel.UnbakedRoot root(BlockStateModel.Unbaked contents) {
        return root(contents, null);
    }

    public static BlockStateModel.UnbakedRoot root(BlockStateModel.Unbaked contents, Memo memo) {
        return new BlockStateModel.UnbakedRoot() {
            @Override
            public void resolveDependencies(ResolvableModel.@NonNull Resolver resolver) {
                contents.resolveDependencies(resolver);
            }

            @Override
            public @NonNull BlockStateModel bake(@NonNull BlockState state, @NonNull ModelBaker baker) {
                return memo == null ? contents.bake(baker) : memo.bake(baker, () -> contents.bake(baker), false);
            }

            @Override
            public @NonNull Object visualEqualityGroup(@NonNull BlockState state) {
                return this;
            }
        };
    }
}
