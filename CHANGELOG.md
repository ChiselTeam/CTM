# Changelog

## [1.21.1.2]

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
