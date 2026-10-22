# CTM Model Format (Minecraft 1.21.1)

CTM uses a NeoForge geometry loader in a **block model JSON**, such as `assets/my_mod/models/block/connected_block_ctm.json`:

```json
"loader": "ctm:connected_texture_model"
```

An ordinary blockstate references this model:

```json
{
  "variants": {
    "": {
      "model": "my_mod:block/connected_block_ctm"
    }
  }
}
```

`model_location` points to a separate texture-source model. It must not point back to the loader model itself. The 26.1.2 root blockstate definition format is not supported by Minecraft 1.21.1.

## Independently connected texture layers

Use `ctm:layered_connected_texture_model` to draw two or more CTM models on the
same block. Entries are rendered in list order (bottom first). Every entry is a
complete CTM definition and therefore computes its connection pattern
independently; layers may use different `kind`, `connected_faces`, and
`connects_to` values.

```json
{
  "loader": "ctm:layered_connected_texture_model",
  "layers": [
    {
      "model_location": "my_mod:block/bottom_layer",
      "connected_faces": [
        "all"
      ],
      "variant": {
        "block": "my_mod:layered_block",
        "kind": "standard"
      },
      "texture_slots": {
        "particle": "my_mod:block/bottom_unconnected",
        "standard_none": "my_mod:block/bottom_unconnected",
        "standard_cornerless": "my_mod:block/bottom_connected_cornerless",
        "standard_vertical": "my_mod:block/bottom_connected_vertical",
        "standard_horizontal": "my_mod:block/bottom_connected_horizontal",
        "standard_corner": "my_mod:block/bottom_connected_corner"
      }
    },
    {
      "model_location": "my_mod:block/top_layer",
      "connected_faces": [
        "all"
      ],
      "variant": {
        "block": "my_mod:layered_block",
        "kind": "standard",
        "water_offset": true
      },
      "texture_slots": {
        "particle": "my_mod:block/top_unconnected",
        "standard_none": "my_mod:block/top_unconnected",
        "standard_cornerless": "my_mod:block/top_connected_cornerless",
        "standard_vertical": "my_mod:block/top_connected_vertical",
        "standard_horizontal": "my_mod:block/top_connected_horizontal",
        "standard_corner": "my_mod:block/top_connected_corner"
      },
      "connects_to": [
        {
          "id": "my_mod:top_connection_target"
        }
      ]
    }
  ]
}
```

The upper textures should contain transparency wherever the lower layer must
remain visible. Setting `water_offset` on the upper layer offsets its quads by
0.01 model units and prevents coplanar z-fighting. The particle texture comes from the first layer. CTM automatically selects render layers per sprite: opaque textures use the solid pass, binary alpha uses cutout, and partial alpha uses the translucent pass. Mixed base layers and overlays are separated in world and inventory rendering. An explicit `render_type` on the outer loader model overrides this automatic selection.

## Minimal example

```json
{
  "loader": "ctm:connected_texture_model",
  "model_location": "my_mod:block/connected_block",
  "connected_faces": [
    "all"
  ],
  "variant": {
    "block": "my_mod:connected_block",
    "kind": "standard"
  }
}
```

## Complete structure

```json
{
  "loader": "ctm:connected_texture_model",
  "model_location": "my_mod:block/connected_block",
  "element": {
    "min": [
      0.0,
      0.0,
      0.0
    ],
    "max": [
      16.0,
      16.0,
      16.0
    ]
  },
  "connected_faces": [
    "all"
  ],
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
    "particle": "my_mod:block/connected_block",
    "standard_none": "my_mod:block/connected_block",
    "standard_cornerless": "my_mod:block/connected_block_ctm_cornerless",
    "standard_vertical": "my_mod:block/connected_block_ctm_vertical",
    "standard_horizontal": "my_mod:block/connected_block_ctm_horizontal",
    "standard_corner": "my_mod:block/connected_block_ctm_corner"
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

### `loader`

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
  "min": [
    0.0,
    0.0,
    0.0
  ],
  "max": [
    16.0,
    16.0,
    16.0
  ]
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

| Kind | Behavior | Required texture states |
|---|---|---|
| `standard` | Four-quadrant connected textures | Five `standard_*` states |
| `tbs` | Independent top, bottom and side connections | Five states for each `standard_top_*`, `standard_bottom_*`, `standard_side_*` group |
| `ar` | Deterministic anti-repeat selection | `ar_1`, `ar_2`, `ar_3`, `ar_4` |
| `bookshelf`, `ctmh` | Horizontal connections on horizontal faces | `horizontal_none`, `horizontal_both`, `horizontal_left`, `horizontal_right` |
| `ctmv` | Vertical connections on horizontal faces | `vertical_none`, `vertical_both`, `vertical_top`, `vertical_bottom` |
| `edges` | Four-quadrant edges with obscured-face support | Five `standard_*` states; optional `overlay_obscured` |
| `edges_full` | Whole-face edge selection | All sixteen `edges_*` states |
| `multiblock_2x2`, `v4`, `r4` | Fixed, provider-based or random 2x2 tiling | Four `multiblock_2x2_*` cells |
| `multiblock_3x3`, `v9`, `r9` | Fixed, provider-based or random 3x3 tiling | Nine `multiblock_3x3_*` cells |
| `multiblock_4x4`, `v16`, `r16` | Fixed, provider-based or random 4x4 tiling | Sixteen `multiblock_4x4_*` cells |

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

Provide textures in the source model's `textures` object or in the loader model's `texture_slots`. All texture states for the selected CTM kind are required. Incomplete sets report the missing states during baking.

`base_texture` is an optional layer beneath the connected textures. `particle` is optional and falls back to a state texture. `top`, `bottom`, and `side` remain available for base layers where supported. Conditional overlays may use any material slot name.

### Standard and regular edges

```json
{
  "standard_none": "my_mod:block/frame_none",
  "standard_cornerless": "my_mod:block/frame_cornerless",
  "standard_vertical": "my_mod:block/frame_vertical",
  "standard_horizontal": "my_mod:block/frame_horizontal",
  "standard_corner": "my_mod:block/frame_corner"
}
```

Each state supplies a full texture. Regular `edges` additionally accepts `overlay_obscured`; it defaults to `standard_none`.

### TBS

Use the same five suffixes under each of `standard_top_`, `standard_bottom_`, and `standard_side_`. All fifteen states are required.

### Edges Full

Use these sixteen slots:

```text
edges_none, edges_diagonals_top_left_bottom_right,
edges_diagonals_top_right_bottom_left, edges_top_left,
edges_corner_bottom_right, edges_bottom, edges_corner_bottom_left,
edges_top_right, edges_right, edges_all, edges_left, edges_right_bottom,
edges_corner_top_right, edges_top, edges_corner_top_left, edges_bottom_left
```

Each slot supplies a full-face texture. `overlay_obscured` is not used by `edges_full`.

### Removed texture-sheet fallbacks

The deprecated combined-sheet slots, atlas UV remappers, and `*_LEGACY` datagen templates have been removed. Resource packs must supply standalone states. Shared image files can expose individual sprites through Minecraft's `minecraft:unstitch` atlas source; the bundled glass and obsidian models use this standard resource format.

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
