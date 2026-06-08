# CTM Data Generation

The CTM library provides a fluent API for generating CTM model JSON files using vanilla Minecraft and NeoForge datagen systems.

## Key Components

- `CTMModelBuilder`: A NeoForge `CustomBlockStateModelBuilder` for configuring CTM model properties.
- `CTMModelTemplates`: Pre-defined vanilla `ModelTemplate`s for different CTM types.
- `CTMTextureSlots`: `TextureSlot` constants for CTM-specific texture keys.
- `CTMBlockStateGenerator`: A vanilla `BlockModelDefinitionGenerator` to link a block to a CTM model.

## Usage Example

The following example shows how to register a CTM model for a block in a `BlockModelGenerators` context:

```java
public void registerModels(BlockModelGenerators blockModels) {
    Block block = MyBlocks.CONNECTED_BLOCK.get();
    
    // 1. Create the texture mapping
    TextureMapping mapping = new TextureMapping()
            .put(TextureSlot.PARTICLE, Identifier.fromNamespaceAndPath("my_mod", "block/my_block"))
            .put(CTMTextureSlots.BASE, Identifier.fromNamespaceAndPath("my_mod", "block/my_block"))
            .put(CTMTextureSlots.OVERLAY_CONNECTED, Identifier.fromNamespaceAndPath("my_mod", "block/my_block_ctm"));

    // 2. Create the base model using a template
    Identifier modelLocation = CTMModelTemplates.STANDARD.create(block, mapping, blockModels.modelOutput);

    // 3. Configure the CTM behavior using the builder
    CTMModelBuilder builder = CTMModelBuilder.standard(block, modelLocation)
            .connectedFace(Direction.UP)
            .connectedFace(Direction.DOWN)
            .connectedFace(Direction.NORTH)
            .connectedFace(Direction.SOUTH)
            .connectedFace(Direction.EAST)
            .connectedFace(Direction.WEST);

    // 4. Register the blockstate
    blockModels.blockStateOutput.accept(CTMBlockStateGenerator.of(block, builder));
}
```

## Supported Factory Methods (CTMModelBuilder)

- `CTMModelBuilder.standard(block, modelLocation)`: Standard 47-state CTM.
- `CTMModelBuilder.tbs(block, modelLocation)`: Top-Bottom-Side CTM.
- `CTMModelBuilder.ar(block, modelLocation)`: Anti-repeat random 2x2 rotation.
- `CTMModelBuilder.bookshelf(block, modelLocation)`: Bookshelf-style horizontal CTM.
- `CTMModelBuilder.horizontal(block, modelLocation)`: Horizontal directional CTM.
- `CTMModelBuilder.vertical(block, modelLocation)`: Vertical directional CTM.
- `CTMModelBuilder.multiblock2x2(block, modelLocation)`: 2x2 multiblock tiling.
- `CTMModelBuilder.multiblock3x3(block, modelLocation)`: 3x3 multiblock tiling.
- `CTMModelBuilder.multiblock4x4(block, modelLocation)`: 4x4 multiblock tiling.

## CTM Model Properties

- `element(Vector3f min, Vector3f max)`: Defines the cuboid for the model (default 0,0,0 to 16,16,16).
- `connectedFace(Direction direction)`: Adds a face that should participate in connection logic.
- `renderOverlayOnAllFaces(boolean)`: Whether to render the overlay even on faces not marked as connected.
- `baseTintIndex(int)` / `tintIndex(int)`: Sets tint indices for the base and overlay layers.
- `eldritch(boolean)`: Enables the eldritch UV-perturbation effect.
- `waterOffset(boolean)`: Nudges the multiblock overlay outward (useful for waterstone-style blocks).

## Example JSON Output

The generated blockstate JSON will look like this:

```json
{
  "loader": "ctm:connected_texture_model",
  "model_location": "my_mod:block/my_block",
  "element": {
    "min": [0.0, 0.0, 0.0],
    "max": [16.0, 16.0, 16.0]
  },
  "connected_faces": ["down", "up", "north", "south", "west", "east"],
  "variant": {
    "block": "my_mod:my_block",
    "kind": "standard"
  }
}
```
