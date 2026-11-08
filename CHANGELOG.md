# Changelog

## [Unreleased]

### Added

- Deferred CTM model baking and lazy standard connected geometry.
- Persistent, synchronized per-block offsets and bounded connected-block offset updates.
- Regression coverage for bake memoization, deferred snapshots, saved offsets, and packet batching.

### Changed

- Eldritch geometry caching follows the delegate geometry key.
- Replaced post-bake quad deduplication with reload-scoped bake caching, preserving null-child safety.
- Adapted deferred material snapshots and section invalidation to the 26.2 APIs.
- Preserved the standalone-texture requirement and existing 26.2 version metadata.

## [26.2.0.2]

### Removed

- Removed all APIs marked `@Deprecated(forRemoval = true)`, including legacy
  model templates, texture-sheet slots, UV remapping, and texture-sheet baking fallbacks.
- Resource packs using the removed sheet layouts must supply standalone state
  textures. See [Model JSON Format](docs/MODELS.md) and [Data Generation](docs/DATAGEN.md).

### Changed

- Converted bundled glass and obsidian models to standalone state textures.
