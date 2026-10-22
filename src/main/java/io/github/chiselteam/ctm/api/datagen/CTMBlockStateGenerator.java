package io.github.chiselteam.ctm.api.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import io.github.chiselteam.ctm.CTM;
import io.github.chiselteam.ctm.client.model.CTMUnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/** Emits both a 1.21.1 blockstate and its separate geometry-loader model. */
public final class CTMBlockStateGenerator implements BlockStateGenerator {
    private final Block block;
    private final CTMUnbakedModel model;
    private final ResourceLocation loader;
    private CTMBlockStateGenerator(Block block, CTMUnbakedModel model, ResourceLocation loader) {
        this.block = block; this.model = model; this.loader = loader;
    }
    public static CTMBlockStateGenerator of(Block block, CTMModelBuilder builder) {
        return new CTMBlockStateGenerator(block, builder.toUnbaked(), CTM.CTM_MODEL_ID);
    }
    public static CTMBlockStateGenerator of(Block block, LayeredCTMModelBuilder builder) {
        return new CTMBlockStateGenerator(block, builder.toUnbaked(), CTM.LAYERED_CTM_MODEL_ID);
    }
    public static CTMBlockStateGenerator layered(Block block, LayeredCTMModelBuilder builder) { return of(block, builder); }
    public ResourceLocation modelLocation() {
        var id = BuiltInRegistries.BLOCK.getKey(block);
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/ctm/" + id.getPath());
    }
    public CTMBlockStateGenerator saveModel(BiConsumer<ResourceLocation, Supplier<JsonElement>> output) {
        output.accept(modelLocation(), this::modelJson);
        return this;
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    public JsonElement modelJson() {
        JsonObject json = ((JsonElement) ((MapCodec) model.codec()).codec().encodeStart(JsonOps.INSTANCE, model).getOrThrow()).getAsJsonObject();
        json.addProperty("loader", loader.toString());
        return json;
    }
    @Override public Block getBlock() { return block; }
    @Override public JsonElement get() {
        JsonObject variant = new JsonObject(); variant.addProperty("model", modelLocation().toString());
        JsonObject variants = new JsonObject(); variants.add("", variant);
        JsonObject json = new JsonObject(); json.add("variants", variants); return json;
    }
}
