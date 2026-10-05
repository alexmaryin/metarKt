# Changelog

All notable changes to this project are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.1 -> 1.2.2]

### Fixed

- **BREAKING** `Wind.componentForRunwayTrue`: the `fromLeft` flag of the returned
  `WindComponent` had inverted semantics. It was `true` when the crosswind came from
  the **right** side of the aircraft. It is now `true` when the wind blows **from the
  pilot's left side**, matching the KDoc and standard aviation convention
  (source-of-wind perspective).
  Callers that compensated for the inversion locally must flip their handling back.
- `Wind.componentForRunwayTrue`: the relative wind angle is now normalized to
  `[-180, 180)` before trigonometric calculations. Previously, the two ends of the same
  physical runway could report differently rounded crosswind components for the same
  wind (e.g. wind `150/05KT`: RWY 18 displayed crosswind `2`, RWY 36 displayed `3`).
  Opposite runway ends now always agree on the crosswind magnitude.

### Changed

- Clarified KDoc for `WindComponent.fromLeft` and
  `Wind.componentForRunwayTrue`: `fromLeft` is `true` when the wind blows from the
  pilot's left side.

### Added

- Unit tests for `componentForRunwayTrue` covering known METAR wind/runway
  combinations, opposite-runway-end consistency, calm wind, variable direction,
  pure headwind/tailwind, and angles wrapping across north.
