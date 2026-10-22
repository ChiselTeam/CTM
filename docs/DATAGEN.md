# CTM Data Generation

The CTM library integrates with Minecraft's `BlockModelGenerators` and NeoForge custom blockstate model system.

Datagen normally performs two separate jobs:

1. Create a block model containing the required texture slots.
2. Register a blockstate whose root definition is `ctm:connected_texture_model`.

## Core classes

- `CTMModelBuilder`: Configures the custom CTM blockstate model.
- `LayeredCTMModelBuilder`: Composes CTM builders in bottom-to-top draw order.
- `CTMBlockStateGenerator`: Registers one CTM root model for every state of a block.
- `CTMModelTemplates`: Convenience `ModelTemplate` instances for common texture layouts.
- `CTMTextureSlots`: Datagen `TextureSlot` constants used by the templates.
- `CTMTextureKeys`: String constants useful with `CTMModelBuilder.texture(...)`.

## Standard CTM example

```java
public void registerModels(BlockModelGenerators blockModels) {
    Block block = MyBlocks.CONNECTED_BLOCK.get();
    Identifier base = Identifier.fromNamespaceAndPath("my_mod", "block/connected_block");

    TextureMapping textures = new TextureMapping()
            .put(TextureSlot.PARTICLE, base)
            .put(CTMTextureSlots.BASE, base)
            .put(CTMTextureSlots.STANDARD_NONE, Identifier.fromNamespaceAndPath("my_mod", "block/connected_none"))
            .put(CTMTextureSlots.STANDARD_CORNERLESS, Identifier.fromNamespaceAndPath("my_mod", "block/connected_cornerless"))
            .put(CTMTextureSlots.STANDARD_VERTICAL, Identifier.fromNamespaceAndPath("my_mod", "block/connected_vertical"))
            .put(CTMTextureSlots.STANDARD_HORIZONTAL, Identifier.fromNamespaceAndPath("my_mod", "block/connected_horizontal"))
            .put(CTMTextureSlots.STANDARD_CORNER, Identifier.fromNamespaceAndPath("my_mod", "block/connected_corner"));

    Identifier modelLocation = CTMModelTemplates.STANDARD.create(block, textures, blockModels.modelOutput);

    CTMModelBuilder builder = CTMModelBuilder.standard(block, modelLocation);

    for (Direction direction : Direction.values())
        builder.connectedFace(direction);

    blockModels.blockStateOutput.accept(CTMBlockStateGenerator.of(block, builder));
}
```

The generated blockstate uses:

```json
"neoforge:definition_type": "ctm:connected_texture_model"
```

The `modelLocation` returned by the template is written to the CTM definition's `model_location` field.

## Direct texture-slot example

`CTMModelBuilder.texture(...)` writes entries into the definition's `texture_slots` object. This can be used to provide or override textures independently of the referenced block model.

```java
CTMModelBuilder builder = CTMModelBuilder.standard(block, modelLocation)
        .texture(CTMTextureKeys.BASE, base)
        .texture(CTMTextureKeys.STANDARD_NONE, Identifier.fromNamespaceAndPath("my_mod", "block/state_none"))
        .texture(CTMTextureKeys.STANDARD_CORNERLESS, Identifier.fromNamespaceAndPath("my_mod", "block/state_cornerless"))
        .texture(CTMTextureKeys.STANDARD_VERTICAL, Identifier.fromNamespaceAndPath("my_mod", "block/state_vertical"))
        .texture(CTMTextureKeys.STANDARD_HORIZONTAL, Identifier.fromNamespaceAndPath("my_mod", "block/state_horizontal"))
        .texture(CTMTextureKeys.STANDARD_CORNER, Identifier.fromNamespaceAndPath("my_mod", "block/state_corner"))
        .texture(CTMTextureKeys.PARTICLE, base);
```

## Layered CTM example

Each layer is a normal `CTMModelBuilder` and supplies its own five standalone
state textures. Layers render in the order passed to `layered(...)`.

```java
CTMModelBuilder bottom = CTMModelBuilder.standard(block, modelLocation)
        .allFaces()
        .standardTextures(bottomNone, bottomCornerless, bottomVertical, bottomHorizontal, bottomCorner)
        .texture(CTMTextureKeys.PARTICLE, bottomNone);

CTMModelBuilder top = CTMModelBuilder.standard(block, modelLocation)
        .allFaces()
        .waterOffset(true)
        .standardTextures(topNone, topCornerless, topVertical, topHorizontal, topCorner)
        .texture(CTMTextureKeys.PARTICLE, topNone);

blockModels.blockStateOutput.accept(CTMBlockStateGenerator.of(block,
        CTMModelBuilder.layered(bottom, top)));
```

The top textures should contain transparency where the bottom layer must remain
visible. Each layer may use its own kind, faces, and connection predicate.

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
builder.standardTextures(none, cornerless, vertical, horizontal, corner);
```

## Model templates

The current `CTMModelTemplates` class includes:

| Template | Required slots |
|---|---|
| `STANDARD` | `particle`, `base_texture`, five `standard_*` states |
| `TBS` | `particle`, `base_texture`, fifteen `standard_top_*` / `standard_bottom_*` / `standard_side_*` states |
| `HORIZONTAL` | `particle`, `base_texture`, four `horizontal_*` states |
| `VERTICAL` | `particle`, `base_texture`, four `vertical_*` states |
| `MULTIBLOCK_2X2` | `particle`, `base_texture`, four `multiblock_2x2_*` cells |
| `MULTIBLOCK_3X3` | `particle`, `base_texture`, nine `multiblock_3x3_*` cells |
| `MULTIBLOCK_4X4` | `particle`, `base_texture`, sixteen `multiblock_4x4_*` cells |
| `AR` | `particle`, `base_texture`, `ar_1` through `ar_4` |
| `EDGES` | `particle`, `base_texture`, five `standard_*` states, `overlay_obscured` |
| `EDGES_FULL` | `particle`, `base_texture`, sixteen `edges_*` states |

Templates require the listed slots at generation time. Runtime base layers are
optional. The `*_LEGACY` templates and texture-sheet slots have been removed.

## Edges example

Regular `edges` uses five standalone states, plus an optional obscured material.

```java
CTMModelBuilder builder = CTMModelBuilder.edges(block, modelLocation)
        .allFaces()
        .standardTextures(none, cornerless, vertical, horizontal, corner)
        .texture(CTMTextureKeys.OVERLAY_OBSCURED, obscured)
        .connectionPredicate(CTMBlockPredicate.any(
                CTMBlockPredicate.block(block),
                CTMBlockPredicate.block(Blocks.NETHER_PORTAL)));

blockModels.blockStateOutput.accept(CTMBlockStateGenerator.of(block, builder));
```

Including the block itself preserves frame-to-frame connections.

## Edges Full example

`edges_full` chooses among sixteen independent full-face textures. Supply the
states in the order accepted by `edgesFullTextures(...)`:

```java
CTMModelBuilder builder = CTMModelBuilder.edgesFull(block, modelLocation)
        .allFaces()
        .edgesFullTextures(none, diagonalsTopLeftBottomRight, diagonalsTopRightBottomLeft,
                topLeft, cornerBottomRight, bottom, cornerBottomLeft, topRight,
                right, all, left, rightBottom, cornerTopRight, top, cornerTopLeft, bottomLeft);

blockModels.blockStateOutput.accept(CTMBlockStateGenerator.of(block, builder));
```

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

builder.texture("overlay_moss", Identifier.fromNamespaceAndPath("my_mod", "block/moss_overlay"));
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

## Generated blockstate shape

A generated CTM blockstate resembles:

```json
{
  "neoforge:definition_type": "ctm:connected_texture_model",
  "model_location": "my_mod:block/connected_block",
  "element": {
    "min": [0.0, 0.0, 0.0],
    "max": [16.0, 16.0, 16.0]
  },
  "connected_faces": ["all"],
  "variant": {
    "block": "my_mod:connected_block",
    "kind": "standard",
    "water_offset": false
  }
}
```
