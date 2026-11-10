# Changelog

## [26.3.0.2]

### Added

- Added the `CTMBlockOffsets` API for per-block texture offsets and bounded updates
  across face-connected blocks of the same type in loaded chunks.
- Added persistent per-dimension offsets, synchronization for tracking players,
  and offset snapshots when players enter affected chunks.
- Added regression tests for deferred baking, caching, material snapshots, and packet batching.

### Changed

- Deferred eligible CTM model baking and standalone Standard geometry until needed
  to reduce upfront baking work and memory use.
- Replaced post-bake quad deduplication with reload-scoped bake caching and shared quads.
- Adapted deferred material snapshots and render-section refreshes to Minecraft 26.3.
- Retained standalone-texture requirements for all CTM models.

### Fixed

- Updated Eldritch geometry caching to follow changes in the underlying connected
  texture geometry instead of reusing parts solely by block position.
- Corrected release game-version metadata and included the changelog on Modrinth.

## [26.3.0.1]

- Updated CTM for Minecraft 26.3 and NeoForge 26.3.0.95.

## [26.2.0.2]

### Removed

- Removed all APIs marked `@Deprecated(forRemoval = true)`, including legacy
  model templates, texture-sheet slots, UV remapping, and texture-sheet baking fallbacks.
- Resource packs using the removed sheet layouts must supply standalone state
  textures. See [Model JSON Format](docs/MODELS.md) and [Data Generation](docs/DATAGEN.md).

### Changed

- Converted bundled glass and obsidian models to standalone state textures.
