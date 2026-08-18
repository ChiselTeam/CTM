# Changelog

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
