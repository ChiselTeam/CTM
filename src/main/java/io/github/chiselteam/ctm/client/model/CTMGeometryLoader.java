package io.github.chiselteam.ctm.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.github.chiselteam.ctm.impl.model.CTMQuadStorage;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public final class CTMGeometryLoader implements IGeometryLoader<CTMGeometryLoader.Geometry> {
    private final Codec<? extends CTMUnbakedModel> codec;

    public CTMGeometryLoader(Codec<? extends CTMUnbakedModel> codec) {
        this.codec = codec;
    }

    @Override
    public @NotNull Geometry read(@NotNull JsonObject json, @NotNull JsonDeserializationContext context) {
        return new Geometry(codec.parse(JsonOps.INSTANCE, json).getOrThrow(JsonParseException::new));
    }

    public record Geometry(CTMUnbakedModel model) implements IUnbakedGeometry<Geometry> {
        @Override
        public void resolveParents(@NotNull Function<ResourceLocation, UnbakedModel> getter, @NotNull IGeometryBakingContext context) {
            model.resolveDependencies(location -> getter.apply(location).resolveParents(getter));
        }

        @Override
        public @NotNull BakedModel bake(@NotNull IGeometryBakingContext context, @NotNull ModelBaker baker, @NotNull Function<Material, TextureAtlasSprite> sprites, @NotNull ModelState state, @NotNull ItemOverrides overrides) {
            var baked = model.bake(new CTMBakingContext(baker, sprites, CTMModelState.of(state), context, context.useAmbientOcclusion()));
            return new Baked(baked, context, overrides);

        }
    }

    static final class Baked extends BakedModelWrapper<CTMBakedModel> implements CTMQuadStorage {
        private final IGeometryBakingContext context;
        private final ItemOverrides overrides;
        private final RenderTypeGroup explicitRenderTypes;
        private volatile List<BakedModel> itemPasses;

        Baked(CTMBakedModel model, IGeometryBakingContext context, ItemOverrides overrides) {
            super(model);
            this.context = context;
            this.overrides = overrides;
            var hint = context.getRenderTypeHint();
            this.explicitRenderTypes = hint == null ? null : context.getRenderType(hint);
        }

        private static RenderType itemRenderType(RenderType layer) {
            if (layer == RenderType.translucent()) return NeoForgeRenderTypes.ITEM_LAYERED_TRANSLUCENT.get();
            if (layer == RenderType.solid()) return NeoForgeRenderTypes.ITEM_LAYERED_SOLID.get();
            return NeoForgeRenderTypes.ITEM_LAYERED_CUTOUT.get();
        }

        @Override
        public void visitChildModels(Consumer<CTMBakedModel> visitor) {
            visitor.accept(originalModel);
        }

        @Override
        public @NotNull ItemTransforms getTransforms() {
            return context.getTransforms();
        }

        @Override
        public @NotNull ItemOverrides getOverrides() {
            return overrides;
        }

        @Override
        public boolean isGui3d() {
            return context.isGui3d();
        }

        @Override
        public boolean usesBlockLight() {
            return context.useBlockLight();
        }

        @Override
        public @NotNull BakedModel applyTransform(@NotNull ItemDisplayContext display, @NotNull PoseStack stack, boolean leftHand) {
            getTransforms().getTransform(display).apply(leftHand, stack);
            return this;
        }

        @Override
        public @NotNull List<BakedQuad> getQuads(
                BlockState state, Direction side,
                @NotNull RandomSource random, @NotNull ModelData data,
                RenderType renderType) {
            var quads = originalModel.getQuads(state, side, random, data, null);
            // Null is the breaking overlay/unfiltered query and must retain every quad.
            if (renderType == null) return quads;
            if (explicitRenderTypes != null) return explicitRenderTypes.block() == renderType ? quads : List.of();
            return quads.stream().filter(quad -> CTMRenderLayers.renderType(quad) == renderType).toList();
        }

        @Override
        public @NotNull ChunkRenderTypeSet getRenderTypes(
                @NotNull BlockState state, @NotNull RandomSource random,
                @NotNull ModelData data) {
            if (explicitRenderTypes != null) return ChunkRenderTypeSet.of(explicitRenderTypes.block());
            var layers = new HashSet<RenderType>();
            for (var face : Direction.values()) {
                for (var quad : originalModel.getQuads(state, face, random, data, null)) layers.add(CTMRenderLayers.renderType(quad));
            }
            for (var quad : originalModel.getQuads(state, null, random, data, null)) layers.add(CTMRenderLayers.renderType(quad));
            return ChunkRenderTypeSet.of(layers);
        }

        @Override
        public @NotNull List<RenderType> getRenderTypes(@NotNull ItemStack stack, boolean fabulous) {
            if (explicitRenderTypes != null) return List.of(fabulous ? explicitRenderTypes.entityFabulous() : explicitRenderTypes.entity());
            // The item renderer gets filtered geometry from getRenderPasses.
            return getRenderTypes(null, RandomSource.create(42), ModelData.EMPTY)
                    .asList().stream().map(Baked::itemRenderType).distinct().toList();
        }

        @Override
        public @NotNull List<BakedModel> getRenderPasses(@NotNull ItemStack stack, boolean fabulous) {
            if (explicitRenderTypes != null) return List.of(this);
            var cached = itemPasses;
            if (cached != null) return cached;
            synchronized (this) {
                if (itemPasses == null) {
                    var passes = new ArrayList<BakedModel>();
                    var layers = getRenderTypes(null, RandomSource.create(42), ModelData.EMPTY);
                    for (var layer : layers) {
                        passes.add(new BakedModelWrapper<Baked>(this) {
                            @Override
                            public @NotNull List<BakedQuad> getQuads(
                                    BlockState state, Direction side, @NotNull RandomSource random) {
                                return originalModel.getQuads(state, side, random, ModelData.EMPTY, layer);
                            }

                            @Override
                            public @NotNull List<RenderType> getRenderTypes(@NotNull ItemStack stack, boolean fabulous) {
                                return List.of(itemRenderType(layer));
                            }
                        });
                    }
                    itemPasses = List.copyOf(passes);
                }
                return itemPasses;
            }
        }
    }
}
