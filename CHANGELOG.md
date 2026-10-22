# Changelog

## [26.2.0.2]

### Removed

- Removed all APIs marked `@Deprecated(forRemoval = true)`, including legacy
  model templates, texture-sheet slots, UV remapping, and texture-sheet baking fallbacks.
- Resource packs using the removed sheet layouts must supply standalone state
  textures. See [Model JSON Format](docs/MODELS.md) and [Data Generation](docs/DATAGEN.md).

### Changed

- Converted bundled glass and obsidian models to standalone state textures.