# CTM Blockstate Model Format

The CTM library uses a NeoForge custom **blockstate model definition**. The CTM definition is placed directly in the block's blockstate JSON file and is identified by:

```json
"neoforge:definition_type": "ctm:connected_texture_model"
```

This is not a vanilla model `loader` field. The `model_location` field points to a separate block model that supplies the texture slots and any model-level properties used while baking.

## Independently connected texture layers

Use `ctm:layered_connected_texture_model` to draw two or more CTM models on the
same block. Entries are rendered in list order (bottom first). Every entry is a
complete CTM definition and therefore computes its connection pattern
independently; layers may use different `kind`, `connected_faces`, and
`connects_to` values.

```json
{
  "neoforge:definition_type": "ctm:layered_connected_texture_model",
  "layers": [
    {
      "model_location": "my_mod:block/bottom_layer",
      "connected_faces": ["all"],
      "variant": {
        "block": "my_mod:layered_block",
        "kind": "standard"
      },
      "texture_slots": {
        "standard_none": "my_mod:block/bottom_unconnected_none",
        "standard_cornerless": "my_mod:block/bottom_unconnected_cornerless",
        "standard_vertical": "my_mod:block/bottom_unconnected_vertical",
        "standard_horizontal": "my_mod:block/bottom_unconnected_horizontal",
        "standard_corner": "my_mod:block/bottom_unconnected_corner",
        "particle": "my_mod:block/bottom_unconnected"
      }
    },
    {
      "model_location": "my_mod:block/top_layer",
      "connected_faces": ["all"],
      "variant": {
        "block": "my_mod:layered_block",
        "kind": "standard",
        "water_offset": true
      },
      "texture_slots": {
        "standard_none": "my_mod:block/top_unconnected_none",
        "standard_cornerless": "my_mod:block/top_unconnected_cornerless",
        "standard_vertical": "my_mod:block/top_unconnected_vertical",
        "standard_horizontal": "my_mod:block/top_unconnected_horizontal",
        "standard_corner": "my_mod:block/top_unconnected_corner",
        "particle": "my_mod:block/top_unconnected"
      },
      "connects_to": [
        { "id": "my_mod:top_connection_target" }
      ]
    }
  ]
}
```

The upper textures should contain transparency wherever the lower layer must
remain visible. Setting `water_offset` on the upper layer offsets its quads by
0.01 model units and prevents coplanar z-fighting. Particle material comes from
the first layer, while material flags are combined from every layer.

## Minimal example

```json
{
  "neoforge:definition_type": "ctm:connected_texture_model",
  "model_location": "my_mod:block/connected_block",
  "connected_faces": ["all"],
  "variant": {
    "block": "my_mod:connected_block",
    "kind": "standard"
  }
}
```

## Complete structure

```json
{
  "neoforge:definition_type": "ctm:connected_texture_model",
  "model_location": "my_mod:block/connected_block",
  "element": {
    "min": [0.0, 0.0, 0.0],
    "max": [16.0, 16.0, 16.0]
  },
  "connected_faces": ["all"],
  "render_overlay_on_all_faces": false,
  "variant": {
    "block": "my_mod:connected_block",
    "kind": "standard",
    "water_offset": false
  },
  "base_tint_index": -1,
  "base_emissivity": 0,
  "tint_index": -1,
  "emissivity": 0,
  "shade": true,
  "eldritch": false,
  "texture_slots": {
    "base_texture": "my_mod:block/connected_block",
    "standard_none": "my_mod:block/connected_block_none",
    "standard_cornerless": "my_mod:block/connected_block_cornerless",
    "standard_vertical": "my_mod:block/connected_block_vertical",
    "standard_horizontal": "my_mod:block/connected_block_horizontal",
    "standard_corner": "my_mod:block/connected_block_corner",
    "particle": "my_mod:block/connected_block"
  },
  "connects_to": [
    {
      "id": "my_mod:connected_block"
    }
  ],
  "overlays": []
}
```

## Fields

### `neoforge:definition_type`

Required. Must be:

```json
"ctm:connected_texture_model"
```

### `model_location`

Required. The resource location of the block model used as the texture and model-property source.

Textures may be declared in that model's normal `textures` object, through inherited texture slots, or directly in this definition using `texture_slots`. Values in `texture_slots` are resolved as texture resource locations.

### `element`

Optional. Defines the rendered cuboid in model coordinates. The default is a full block:

```json
{
  "min": [0.0, 0.0, 0.0],
  "max": [16.0, 16.0, 16.0]
}
```

### `connected_faces`

Required. Controls which rendered faces participate in CTM connection logic.

All faces:

```json
"connected_faces": ["all"]
```

Selected faces:

```json
"connected_faces": ["north", "south", "east", "west"]
```

Valid directions are `down`, `up`, `north`, `south`, `west`, and `east`.

### `render_overlay_on_all_faces`

Optional, default `false`. When enabled, the CTM overlay may be rendered on faces outside `connected_faces`. Those faces use the unconnected/default pattern because no connection key is calculated for them.

### `variant`

Required.

```json
"variant": {
  "block": "my_mod:connected_block",
  "kind": "standard",
  "water_offset": false
}
```

- `block`: The target block represented by this CTM model.
- `kind`: The CTM algorithm.
- `water_offset`: Optional. Offsets applicable overlay geometry slightly outward. It is primarily used by multiblock/water-style textures to avoid coplanar rendering artifacts.

## Supported CTM kinds

| Kind | Behavior | Standalone texture slots |
|---|---|---|
| `standard` | Four-quadrant CTM | Five `standard_*` states |
| `tbs` | Separate top, bottom, and side CTM | Five states each for `standard_top_*`, `standard_bottom_*`, `standard_side_*` |
| `ar` | Deterministic anti-repeat selection | `ar_1` through `ar_4` |
| `bookshelf`, `ctmh` | Horizontal connections on horizontal faces | `horizontal_none`, `horizontal_both`, `horizontal_left`, `horizontal_right` |
| `ctmv` | Vertical connections on horizontal faces | `vertical_none`, `vertical_both`, `vertical_top`, `vertical_bottom` |
| `edges` | Four-quadrant edge CTM with obscured-face support | Five `standard_*` states; optional `overlay_obscured` |
| `edges_full` | One of sixteen full-face edge states | Sixteen `edges_*` states |
| `multiblock_2x2`, `v4`, `r4` | Fixed, provider-based, or random 2x2 tiling | Four `multiblock_2x2_*` cells |
| `multiblock_3x3`, `v9`, `r9` | Fixed, provider-based, or random 3x3 tiling | Nine `multiblock_3x3_*` cells |
| `multiblock_4x4`, `v16`, `r16` | Fixed, provider-based, or random 4x4 tiling | Sixteen `multiblock_4x4_row_N_column_N` cells |

## Tint, emissivity, and effects

All fields are optional:

- `base_tint_index`: Tint index for the base layer. Default `-1`.
- `base_emissivity`: Emissivity for the base layer. Default `0`.
- `tint_index`: Tint index for the CTM overlay. Default `-1`.
- `emissivity`: Emissivity for the CTM overlay. Default `0`.
- `shade`: Enables Minecraft's directional diffuse face shading for every generated base and overlay quad. Default `true`. Set to `false` for uniformly colored faces; this is independent of emissivity.
- `eldritch`: Wraps the baked model in the eldritch UV-transforming model. Default `false`.

The JSON field is `tint_index`, not `tintIndex`.

## Texture slots

`texture_slots` is optional. It can provide or override textures without requiring them to exist in the model referenced by `model_location`.

```json
"texture_slots": {
  "base_texture": "my_mod:block/base",
  "standard_none": "my_mod:block/unconnected_none",
  "standard_cornerless": "my_mod:block/unconnected_cornerless",
  "standard_vertical": "my_mod:block/unconnected_vertical",
  "standard_horizontal": "my_mod:block/unconnected_horizontal",
  "standard_corner": "my_mod:block/unconnected_corner",
  "particle": "my_mod:block/base"
}
```

### Common slots

- `base_texture`: Optional base layer; model `all` and `layer0` slots remain base fallbacks.
- `particle`: Optional particle texture; defaults to an available base or state texture.
- `top`, `bottom`, `side`: Face-specific base textures for TBS (`top` and `bottom` also apply to directional CTM).
- `overlay_obscured`: Optional obscured-face material for regular `edges`.
- Standalone state slots listed in the table above.

Custom conditional overlay materials may use any additional slot name.

## Texture expectations by model type

Each connection state uses a separate texture. Supply the complete state set for
the selected kind. Legacy texture sheets and their UV remapping have been removed.

### Standard

The five slots are `standard_none`, `standard_cornerless`, `standard_vertical`,
`standard_horizontal`, and `standard_corner`. Each texture uses the model's
original UV coordinates; no sheet subdivision is applied. `base_texture` may be
omitted when the state textures provide the entire visible surface.

### TBS

Supply those five states for each face group, using `standard_top_*`,
`standard_bottom_*`, and `standard_side_*`. For example, `standard_top_none`
and `standard_top_corner` select the unconnected and corner states of the top face.
Optional `top`, `bottom`, and `side` base textures fall back to `base_texture`.

### Edges

Regular `edges` uses the same five standalone states as Standard, selecting each
quadrant independently. The optional full-face `overlay_obscured` texture is used
when a matching block covers the rendered face; it defaults to `standard_none`.

Edges checks connections in the rendered face plane and one block in front of
that plane, allowing a frame to connect to geometry occupying its opening.

### Edges Full

`edges_full` uses sixteen separate `edges_*` textures, each covering a whole face.
Use `CTMModelBuilder.edgesFullTextures(...)` to supply the full state set in lookup
order, or the constants in `CTMTextureKeys`. `edges_none` is the unconnected state;
obscured faces use `edges_all`. `overlay_obscured` is unused for this kind.

## Custom connection rules: `connects_to`

When omitted, the model uses `CTMBlockPredicate.sameBlock()` and connects to the target block in `variant.block`.

Providing `connects_to` replaces that default predicate.

```json
"connects_to": [
  {
    "id": "minecraft:stone"
  },
  {
    "id": "minecraft:oak_log",
    "state": {
      "axis": "y"
    }
  }
]
```

Each entry contains:

- `id`: Required block ID.
- `state`: Optional property/value map. Every listed property must match.

Multiple entries use OR logic. An empty or invalid property name/value causes model decoding to fail rather than silently matching.

Because an explicit `connects_to` list replaces same-block matching, include the target block in the list when the model must connect both to itself and to another block:

```json
"connects_to": [
  { "id": "my_mod:frame" },
  { "id": "minecraft:nether_portal" }
]
```

## Conditional overlays: `overlays`

Conditional overlays render an additional full-face material when their conditions match.

```json
"overlays": [
  {
    "material": "overlay_moss",
    "faces": ["north", "south", "east", "west"],
    "conditions": {
      "self": {
        "id": "my_mod:connected_block",
        "state": {
          "waterlogged": false
        }
      },
      "neighbors": [
        {
          "direction": "up",
          "id": "minecraft:moss_block"
        }
      ]
    },
    "priority": 10,
    "tint_index": -1,
    "emissivity": 0
  }
]
```

### Overlay fields

- `material`: Required texture-slot name.
- `faces`: Optional; defaults to all faces. Supports `["all"]`.
- `conditions`: Optional; no conditions means the rule always matches.
- `priority`: Optional, default `0`. Rules are baked in priority order.
- `tint_index`: Optional, default `-1`.
- `emissivity`: Optional, default `0`.

### `self`

Checks the modeled block's current state:

```json
"self": {
  "id": "my_mod:connected_block",
  "state": {
    "lit": true
  }
}
```

### `neighbors`

Checks neighboring block appearances.

Specific direction:

```json
"neighbors": [
  {
    "direction": "down",
    "id": "minecraft:stone"
  }
]
```

Any of the six directions:

```json
"neighbors": [
  {
    "direction": "any",
    "id": "minecraft:moss_block"
  }
]
```

Omitting `direction` is equivalent to `"direction": "any"`.

All entries inside one `conditions` object use AND logic. An `any` neighbor entry succeeds when at least one of the six adjacent blocks matches its matcher.
