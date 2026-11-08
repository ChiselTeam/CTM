# Changelog

## [Unreleased - 1.21.1]

### Added
- Added per-block texture offsets, connected-block propagation, dimension persistence,
  and chunk-tracking synchronization.
- Deferred eligible CTM baking and full-cube Standard geometry until first use.

### Changed
- Added bake-scoped memoization and structural quad sharing for the 1.21 model pipeline.
- Preserved item transforms, render passes, and per-layer transparency selection.

### Fixed
- Skip null model children while canonicalizing baked quads.
- Avoid Eldritch cache reuse for null geometry keys and unknown dynamic delegates.
- Invalidate removed offsets when replacing a chunk snapshot, including an empty snapshot.

## [26.1.2.9]

### Added
- Added support for separate textures for Standard, Horizontal, Vertical, Multiblock,
  AR, TBS, and Edges CTM models, with new texture slots, model templates, and datagen helpers.

### Changed
- Updated CTM model baking to use separate textures while retaining legacy texture-sheet fallback.

## [26.1.2.8]

- Fixed directional connected textures flipping depending on the block axis.
- Preserved consistent connected-face ordering.

## [26.1.2.7]

- Added support for model rotation across CTM models and datagen builders.
- Updated NeoForge to 26.1.2.84.

## [26.1.2.6]

- Added configurable ambient occlusion for CTM models and datagen builders.

## [26.1.2.5]

- Added configurable shading for CTM models and datagen builders.

## [26.1.2.4]

- Added `layered_connected_texture_model`, allowing multiple CTM layers on the
  same face to calculate and render their connections independently.

## [26.1.2.3]

### Added
- Added `EDGES` and `EDGES_FULL` CTM kinds, allowing for single-face overlays based on neighbor connections.
- Added support for "obscured" overlays in Edges CTM via the `OVERLAY_OBSCURED` texture key.
- Introduced `CTMOverlayCondition` and `CTMOverlayRule` API for more flexible overlay rendering logic.
- Added `CTMModelBuilder.of` factory method to simplify creating builders for any `CTMKind`.
- Added new model templates for `EDGES` and `EDGES_FULL` in `CTMModelTemplates`.
- Added comprehensive documentation for CTM Data Generation (`DATAGEN.md`) and Model JSON formats (`MODELS.md`).

### Changed
- Improved datagen API with better support for custom model loaders and templates.
