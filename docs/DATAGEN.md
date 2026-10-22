# CTM Data Generation

The CTM library integrates with Minecraft 1.21.1's model and blockstate output consumers.

Datagen normally performs two separate jobs:

1. Create a block model containing the required texture slots.
2. Emit a geometry-loader model with `saveModel(modelOutput)`, then register a vanilla blockstate referencing it.

## Core classes

- `CTMModelBuilder`: Configures the custom CTM blockstate model.
- `LayeredCTMModelBuilder`: Composes CTM builders in bottom-to-top draw order.
- `CTMBlockStateGenerator`: Emits a vanilla blockstate referencing a separate CTM model.
- `CTMModelTemplates`: Convenience `ModelTemplate` instances for common texture layouts.
- `CTMTextureSlots`: Datagen `TextureSlot` constants used by the templates.
- `CTMTextureKeys`: String constants useful with `CTMModelBuilder.texture(...)`.

In 1.21.1, use `net.minecraft.data.models.blockstates.BlockStateGenerator` and `net.minecraft.data.models.model.*`. Supply the `Consumer<BlockStateGenerator>` and `BiConsumer<ResourceLocation, Supplier<JsonElement>>` from your data provider; the vanilla `BlockModelGenerators` output fields are not public. CTM builders are standalone builders in this version, rather than subclasses of the newer NeoForge custom blockstate builder.

`saveModel` writes `block/ctm/<block_path>`. Keep the texture-source model at a different location so dependencies cannot point back to the loader model. Item model JSON can reference the generated loader model as its parent.

## Standard CTM example

```java
public void registerModels(Consumer<BlockStateGenerator> blockStateOutput,
        BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput) {
    Block block = MyBlocks.CONNECTED_BLOCK.get();
    ResourceLocation source = ResourceLocation.fromNamespaceAndPath("my_mod", "block/connected_block");
    CTMModelBuilder builder = CTMModelBuilder.standard(block, source)
            .allFaces()
            .baseTexture(source)
            .particleTexture(source)
            .standardTextures(source);
    blockStateOutput.accept(CTMBlockStateGenerator.of(block, builder).saveModel(modelOutput));
}
```

`standardTextures(source)` supplies `standard_none`, `standard_cornerless`, `standard_vertical`, `standard_horizontal`, and `standard_corner` using the source path with the corresponding suffix. Supply all five texture files, or use the overload taking five explicit resource locations. The separate `source` model must exist, even when all textures are supplied by the builder.

## Direct texture slots

Use `builder.texture(slot, texture)` for individual state textures or named conditional overlays. `baseTexture` and `particleTexture` remain optional; the particle falls back to an available state texture.

## Layered CTM example

```java
CTMModelBuilder bottom = CTMModelBuilder.standard(block, source)
        .allFaces().standardTextures(bottomTextureBase);
CTMModelBuilder top = CTMModelBuilder.standard(block, source)
        .allFaces().waterOffset(true).standardTextures(topTextureBase);
LayeredCTMModelBuilder layered = CTMModelBuilder.layered(bottom, top);
blockStateOutput.accept(CTMBlockStateGenerator.of(block, layered).saveModel(modelOutput));
```

Each layer has its own complete state-texture set and connection calculation. Upper textures may contain transparency; `waterOffset(true)` moves their quads outward to avoid z-fighting.

## Available builder factories

The current `CTMModelBuilder` provides:

```java
CTMModelBuilder.standard(block, modelLocation);
CTMModelBuilder.tbs(block, modelLocation);
CTMModelBuilder.ar(block, modelLocation);
CTMModelBuilder.bookshelf(block, modelLocation);
CTMModelBuilder.horizontal(block, modelLocation);
CTMModelBuilder.vertical(block, modelLocation);
CTMModelBuilder.edges(block, modelLocation);
CTMModelBuilder.edgesFull(block, modelLocation);
CTMModelBuilder.multiblock2x2(block, modelLocation);
CTMModelBuilder.multiblock3x3(block, modelLocation);
CTMModelBuilder.multiblock4x4(block, modelLocation);
```

The runtime also supports `v4`, `v9`, `v16`, `r4`, `r9`, and `r16`, but the current builder does not expose dedicated factory methods for them.

## Builder options

### Geometry and faces

```java
builder.element(new Vector3f(0, 0, 0), new Vector3f(16, 16, 16));
builder.connectedFace(Direction.NORTH);
builder.renderOverlayOnAllFaces(true);
```

- `element(min, max)`: Sets the rendered cuboid. Default is a full block.
- `connectedFace(direction)`: Adds one face to connection calculations.
- `renderOverlayOnAllFaces(value)`: Renders an unconnected/default overlay on faces not participating in connection calculations.

- `allFaces()`: Adds all six directions to `connected_faces`.
- Use repeated `connectedFace(...)` calls when only selected faces should connect.

### Tint and emissivity

```java
builder.baseTintIndex(-1);
builder.baseEmissivity(0);
builder.tintIndex(-1);
builder.emissivity(0);
builder.shade(false);
```

- `shade(value)`: Controls directional diffuse shading on every generated base and overlay quad. It defaults to `true` and is independent of the emissivity settings.

### Effects and offsets

```java
builder.eldritch(true);
builder.waterOffset(true);
```

- `eldritch(true)`: Wraps the baked result in the eldritch UV transformer.
- `waterOffset(true)`: Enables the variant's outward overlay offset behavior where supported.

### Connections and overlays

```java
builder.connectionPredicate(predicate);
builder.overlay(rule);
```

### Direct texture slots

```java
builder.texture(CTMTextureKeys.BASE, baseTexture);
builder.texture(CTMTextureKeys.STANDARD_CORNER, cornerTexture);
```

## Model templates

Templates create texture-source models. Emit the CTM loader model separately with `saveModel`.

| Template | Required state slots, in addition to `particle` and `base_texture` |
|---|---|
| `STANDARD` | Five `standard_*` states |
| `TBS` | Five states for each of `standard_top_*`, `standard_bottom_*`, `standard_side_*` |
| `HORIZONTAL` | `horizontal_none`, `horizontal_both`, `horizontal_left`, `horizontal_right` |
| `VERTICAL` | `vertical_none`, `vertical_both`, `vertical_top`, `vertical_bottom` |
| `MULTIBLOCK_2X2` | Four `multiblock_2x2_*` cells |
| `MULTIBLOCK_3X3` | Nine `multiblock_3x3_*` cells |
| `MULTIBLOCK_4X4` | Sixteen `multiblock_4x4_*` cells |
| `AR` | `ar_1`, `ar_2`, `ar_3`, `ar_4` |
| `EDGES` | Five `standard_*` states and `overlay_obscured` |
| `EDGES_FULL` | Sixteen `edges_*` states |

## Edges examples

```java
CTMModelBuilder regular = CTMModelBuilder.edges(block, source)
        .allFaces().baseTexture(base).standardTextures(edgeTextureBase)
        .obscuredTexture(obscured);
CTMModelBuilder full = CTMModelBuilder.edgesFull(block, source)
        .allFaces().baseTexture(base).edgesFullTextures(edgeTextureBase);
```

`edgesFullTextures` supplies all sixteen semantic edge states. Each state is a separate full-face texture. Missing state sets fail model baking with a list of missing states.

## Custom connection predicates

### Same block

This is the builder default:

```java
builder.connectionPredicate(CTMBlockPredicate.sameBlock());
```

### One block

```java
builder.connectionPredicate(CTMBlockPredicate.block(Blocks.STONE));
```

### One block state

```java
ResolvedBlockStateMatcher verticalLogs = ResolvedBlockStateMatcher.forBlock(Blocks.OAK_LOG)
        .with(BlockStateProperties.AXIS, Direction.Axis.Y)
        .build();

builder.connectionPredicate(CTMBlockPredicate.state(verticalLogs));
```

### Any listed predicate

```java
builder.connectionPredicate(CTMBlockPredicate.any(
        CTMBlockPredicate.block(block),
        CTMBlockPredicate.block(Blocks.STONE),
        CTMBlockPredicate.state(verticalLogs)
));
```

### All listed predicates

```java
builder.connectionPredicate(CTMBlockPredicate.all(firstPredicate, secondPredicate));
```

`all(...)` exists in the Java API, although the current JSON `connects_to` codec represents its list as OR logic.

## Conditional overlay example

```java
ResolvedBlockStateMatcher moss = ResolvedBlockStateMatcher.forBlock(Blocks.MOSS_BLOCK).build();

CTMModelCodecs.UnbakedOverlayRule mossRule = new CTMModelCodecs.UnbakedOverlayRule(
        "overlay_moss",
        EnumSet.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST),
        new CTMOverlayConditions.NeighborCondition(Direction.UP, moss),
        10,
        -1,
        0
);

builder.texture("overlay_moss", ResourceLocation.fromNamespaceAndPath("my_mod", "block/moss_overlay"));
builder.overlay(mossRule);
```

The constructor values are:

```text
material, faces, condition, priority, tintIndex, emissivity
```

### Any-neighbor condition

```java
new CTMOverlayConditions.AnyNeighborCondition(moss)
```

### Multiple required conditions

```java
new CTMOverlayConditions.AllOfOverlayCondition(List.of(firstCondition, secondCondition))
```

### Any matching condition

```java
new CTMOverlayConditions.AnyOfOverlayCondition(List.of(firstCondition, secondCondition))
```

The Java API supports `AnyOfOverlayCondition`; the current JSON condition codec combines `self` and `neighbors` using AND logic.

## Generated files

The loader model at `models/block/ctm/<block_path>.json` contains `loader`, `model_location`, `variant`, and the builder settings. Its `texture_slots` contain the standalone states.

The separate blockstate references it:

```json
{ "variants": { "": { "model": "my_mod:block/ctm/connected_block" } } }
```
