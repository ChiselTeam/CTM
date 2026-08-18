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
        "overlay_texture": "my_mod:block/bottom_unconnected",
        "overlay_connected": "my_mod:block/bottom_connected",
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
        "overlay_texture": "my_mod:block/top_unconnected",
        "overlay_connected": "my_mod:block/top_connected",
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
  "eldritch": false,
  "texture_slots": {
    "base_texture": "my_mod:block/connected_block",
    "overlay_texture": "my_mod:block/connected_block",
    "overlay_connected": "my_mod:block/connected_block_ctm",
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

| Kind | Behavior | Primary overlay slot |
|---|---|---|
| `standard` | Standard four-quadrant CTM | `overlay_connected` |
| `tbs` | Separate top, bottom, and side CTM | `overlay_top_connected`, `overlay_bottom_connected`, `overlay_side_connected` |
| `ar` | Deterministic anti-repeat 2×2 selection | `overlay_2x2` |
| `bookshelf` | Horizontal bookshelf-style connection on horizontal faces | `overlay_horizontal` |
| `ctmh` | Horizontal directional CTM | `overlay_horizontal` |
| `ctmv` | Vertical directional CTM | `overlay_vertical` |
| `edges` | Four-quadrant edge CTM with obscured-face support | `overlay_connected` |
| `edges_full` | Selects one cell from a 4×4 atlas and stretches it over the full face | `overlay_connected` |
| `multiblock_2x2` | Fixed 2×2 tiling | `overlay_2x2` |
| `multiblock_3x3` | Fixed 3×3 tiling | `overlay_3x3` |
| `multiblock_4x4` | Fixed 4×4 tiling | `overlay_4x4` |
| `v4` | Deterministic provider-based 2×2 multiblock | `overlay_2x2` |
| `v9` | Deterministic provider-based 3×3 multiblock | `overlay_3x3` |
| `v16` | Deterministic provider-based 4×4 multiblock | `overlay_4x4` |
| `r4` | Random per-position 2×2 multiblock | `overlay_2x2` |
| `r9` | Random per-position 3×3 multiblock | `overlay_3x3` |
| `r16` | Random per-position 4×4 multiblock | `overlay_4x4` |

## Tint, emissivity, and effects

All fields are optional:

- `base_tint_index`: Tint index for the base layer. Default `-1`.
- `base_emissivity`: Emissivity for the base layer. Default `0`.
- `tint_index`: Tint index for the CTM overlay. Default `-1`.
- `emissivity`: Emissivity for the CTM overlay. Default `0`.
- `eldritch`: Wraps the baked model in the eldritch UV-transforming model. Default `false`.

The JSON field is `tint_index`, not `tintIndex`.

## Texture slots

`texture_slots` is optional. It can provide or override textures without requiring them to exist in the model referenced by `model_location`.

```json
"texture_slots": {
  "base_texture": "my_mod:block/base",
  "overlay_texture": "my_mod:block/unconnected",
  "overlay_connected": "my_mod:block/connected",
  "particle": "my_mod:block/base"
}
```

### Common slots

- `base_texture`
- `overlay_texture`
- `overlay_connected`
- `overlay_obscured`
- `overlay_top`
- `overlay_bottom`
- `overlay_side`
- `overlay_top_connected`
- `overlay_bottom_connected`
- `overlay_side_connected`
- `overlay_horizontal`
- `overlay_vertical`
- `overlay_2x2`
- `overlay_3x3`
- `overlay_4x4`
- `top`
- `bottom`
- `side`
- `particle`
- `layer0` and `layer1` as compatibility fallbacks in supported model types

Custom overlay materials may use any additional slot name.

## Texture expectations by model type

### Standard

```json
"texture_slots": {
  "base_texture": "my_mod:block/base",
  "overlay_texture": "my_mod:block/unconnected",
  "overlay_connected": "my_mod:block/connected",
  "particle": "my_mod:block/base"
}
```

`base_texture` is optional if the overlay is intended to provide the entire visible texture. The standard implementation also recognizes `layer0` and `layer1` as fallbacks.

### TBS

TBS accepts face-specific base and connected overlay textures:

```json
"texture_slots": {
  "top": "my_mod:block/top",
  "bottom": "my_mod:block/bottom",
  "side": "my_mod:block/side",
  "overlay_top": "my_mod:block/top_unconnected",
  "overlay_bottom": "my_mod:block/bottom_unconnected",
  "overlay_side": "my_mod:block/side_unconnected",
  "overlay_top_connected": "my_mod:block/top_connected",
  "overlay_bottom_connected": "my_mod:block/bottom_connected",
  "overlay_side_connected": "my_mod:block/side_connected",
  "particle": "my_mod:block/side"
}
```

General `base_texture`, `overlay_texture`, and `overlay_connected` slots are used as fallbacks where supported.

### Edges

Regular `edges` splits the face into four quadrants and selects each quadrant independently.

```json
"texture_slots": {
  "base_texture": "my_mod:block/frame",
  "overlay_texture": "my_mod:block/edges_unconnected",
  "overlay_connected": "my_mod:block/edges_connected",
  "overlay_obscured": "my_mod:block/edges_obscured",
  "particle": "my_mod:block/frame"
}
```

- `overlay_texture`: Unconnected/default edge texture.
- `overlay_connected`: Connected edge texture.
- `overlay_obscured`: Optional full-face texture used when a matching block directly covers the rendered face. It falls back to `overlay_texture`.

Edges checks connections in the rendered face plane and one block in front of that plane. This allows a frame block to connect to geometry occupying the framed opening, such as an active portal, without treating an empty opening as connected.

### Edges Full

`edges_full` uses a 4×4 atlas in `overlay_connected`. One 4×4-pixel cell of a 16×16 texture is stretched over the full block face.

```json
"texture_slots": {
  "base_texture": "my_mod:block/frame",
  "overlay_texture": "my_mod:block/edges_default",
  "overlay_connected": "my_mod:block/edges_full_atlas",
  "particle": "my_mod:block/frame"
}
```

`overlay_texture` is used for the no-connection pattern. Obscured faces use the connected atlas's center/full pattern; `overlay_obscured` is not used by `edges_full`.

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
