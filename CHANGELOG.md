# Changelog

## Unreleased - 26.3

### Added

- Deferred CTM baking and lazy standalone Standard geometry, with shared quad caching.
- Per-block texture offsets with persistent storage, connected-block updates, and chunk-watch synchronization.
- Regression coverage for bake memoization, deferred realization, snapshot materials, and packet batching.

### Changed

- Eldritch caching now tracks delegate geometry changes at each block position.
- Adapted deferred material snapshots and render-section refreshes to Minecraft 26.3 APIs while preserving shading-direction overrides and standalone-only textures.

## [26.2.0.2]

### Removed

- Removed all APIs marked `@Deprecated(forRemoval = true)`, including legacy
  model templates, texture-sheet slots, UV remapping, and texture-sheet baking fallbacks.
- Resource packs using the removed sheet layouts must supply standalone state
  textures. See [Model JSON Format](docs/MODELS.md) and [Data Generation](docs/DATAGEN.md).

### Changed

- Converted bundled glass and obsidian models to standalone state textures.