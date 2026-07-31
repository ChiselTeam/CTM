# CTM Data Generation

The CTM library integrates with Minecraft's `BlockModelGenerators` and NeoForge custom blockstate model system.

Datagen normally performs two separate jobs:

1. Create a block model containing the required texture slots.
2. Register a blockstate whose root definition is `ctm:connected_texture_model`.

## Core classes

- `CTMModelBuilder`: Configures the custom CTM blockstate model.
- `CTMBlockStateGenerator`: Registers one CTM root model for every state of a block.
- `CTMModelTemplates`: Convenience `ModelTemplate` instances for common texture layouts.
- `CTMTextureSlots`: Datagen `TextureSlot` constants used by the templates.
- `CTMTextureKeys`: String constants useful with `CTMModelBuilder.texture(...)`.

## Standard CTM example

```java
public void registerModels(BlockModelGenerators blockModels) {
    Block block = MyBlocks.CONNECTED_BLOCK.get();
    Identifier base = Identifier.fromNamespaceAndPath("my_mod", "block/connected_block");
    Identifier connected = Identifier.fromNamespaceAndPath("my_mod", "block/connected_block_ctm");

    TextureMapping textures = new TextureMapping()
            .put(TextureSlot.PARTICLE, base)
            .put(CTMTextureSlots.BASE, base)
            .put(CTMTextureSlots.OVERLAY_CONNECTED, connected);

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
        .texture(CTMTextureKeys.OVERLAY, unconnected)
        .texture(CTMTextureKeys.OVERLAY_CONNECTED, connected)
        .texture(CTMTextureKeys.PARTICLE, base);
```

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

There is currently no `connectedFaces(...)` or `allFaces()` convenience method, so add all six directions with a loop when needed.

### Tint and emissivity

```java
builder.baseTintIndex(-1);
builder.baseEmissivity(0);
builder.tintIndex(-1);
builder.emissivity(0);
```

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
builder.texture(CTMTextureKeys.OVERLAY_CONNECTED, connectedTexture);
```

## Model templates

The current `CTMModelTemplates` class includes:

| Template | Required slots |
|---|---|
| `STANDARD` | `particle`, `base_texture`, `overlay_connected` |
| `TBS` | `particle`, `top`, `bottom`, `side`, `overlay_connected` |
| `HORIZONTAL` | `particle`, `base_texture`, `overlay_horizontal` |
| `VERTICAL` | `particle`, `base_texture`, `overlay_vertical` |
| `MULTIBLOCK_2X2` | `particle`, `base_texture`, `overlay_2x2` |
| `MULTIBLOCK_3X3` | `particle`, `base_texture`, `overlay_3x3` |
| `MULTIBLOCK_4X4` | `particle`, `base_texture`, `overlay_4x4` |
| `AR` | `particle`, `base_texture`, `overlay_texture` |
| `EDGES` | `particle`, `base_texture`, `overlay_texture`, `overlay_connected`, `overlay_obscured` |
| `EDGES_FULL` | `particle`, `base_texture`, `overlay_texture`, `overlay_connected` |

A template declares required texture slots for datagen. Runtime model implementations may support additional fallback slots that are not represented by a particular template.

## Edges example

Regular `edges` uses independent quadrant selection and optionally supports a special obscured-face texture.

```java
public void createEdges(Block block, BlockModelGenerators blockModels) {
    Identifier base = Identifier.fromNamespaceAndPath("my_mod", "block/portal_frame");
    Identifier unconnected = Identifier.fromNamespaceAndPath("my_mod", "block/portal_edges_unconnected");
    Identifier connected = Identifier.fromNamespaceAndPath("my_mod", "block/portal_edges_connected");
    Identifier obscured = Identifier.fromNamespaceAndPath("my_mod", "block/portal_edges_obscured");

    TextureMapping textures = new TextureMapping()
            .put(TextureSlot.PARTICLE, base)
            .put(CTMTextureSlots.BASE, base)
            .put(CTMTextureSlots.OVERLAY, unconnected)
            .put(CTMTextureSlots.OVERLAY_CONNECTED, connected)
            .put(CTMTextureSlots.OVERLAY_OBSCURED, obscured);

    Identifier modelLocation = CTMModelTemplates.EDGES.create(block, textures, blockModels.modelOutput);

    CTMBlockPredicate connectsTo = CTMBlockPredicate.any(
            CTMBlockPredicate.block(block),
            CTMBlockPredicate.block(Blocks.NETHER_PORTAL)
    );

    CTMModelBuilder builder = CTMModelBuilder.edges(block, modelLocation)
            .connectionPredicate(connectsTo);

    for (Direction direction : Direction.values())
        builder.connectedFace(direction);

    blockModels.blockStateOutput.accept(CTMBlockStateGenerator.of(block, builder));
}
```

Including `block` in the explicit predicate preserves frame-to-frame connections. Omitting it means the model connects only to the other listed blocks.

## Edges Full example

`edges_full` maps one cell of a 4×4 connected atlas across the complete face.

```java
public void createEdgesFull(Block block, BlockModelGenerators blockModels) {
    Identifier base = Identifier.fromNamespaceAndPath("my_mod", "block/frame");
    Identifier defaultEdges = Identifier.fromNamespaceAndPath("my_mod", "block/edges_default");
    Identifier atlas = Identifier.fromNamespaceAndPath("my_mod", "block/edges_full_atlas");

    TextureMapping textures = new TextureMapping()
            .put(TextureSlot.PARTICLE, base)
            .put(CTMTextureSlots.BASE, base)
            .put(CTMTextureSlots.OVERLAY, defaultEdges)
            .put(CTMTextureSlots.OVERLAY_CONNECTED, atlas);

    Identifier modelLocation = CTMModelTemplates.EDGES_FULL.create(block, textures, blockModels.modelOutput);
    CTMModelBuilder builder = CTMModelBuilder.edgesFull(block, modelLocation);

    for (Direction direction : Direction.values())
        builder.connectedFace(direction);

    blockModels.blockStateOutput.accept(CTMBlockStateGenerator.of(block, builder));
}
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
