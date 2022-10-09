# Iris 1.4 Changelog (full)

Iris 1.4 is now available for download for 1.16.5, 1.18.2, and 1.19.2, adding many OptiFine parity features as well as new features for shader developers.
This release also includes a few bug fixes and DSA support for performance improvements.

This release was developed by douira and IMS.

**There is a new document for debugging information; see [debugging.md](/docs/usage/debugging.md).**

## Features

- Added GLSL Transformer (douira)
- Added custom buffer size support (IMS)
- Added custom per-buffer blending support (IMS)
- Added compute shader support (IMS)
- Added feature flags, see [Feature flags](#feature-flags). (IMS)
- Added `entity_translucent` shader to render translucent entities differently. (IMS)
- Added a button to hide the settings GUI (ConsoleLogLuke)

## Bug fixes and mod compatibility improvements

- Use the camera position instead of the player position when possible for the eyePosition uniform (parzivail)
- Rewrite 1.16.x horizon rendering to work similarly to post 1.18 (IMS)
- Added Sildur's BiomeTemp uniform (IMS)

## Feature Flags
Feature flags are a new system in Iris to query the existence of certain features. To activate them use `iris.feature.required` to show an error if your PC or Iris doesn't support a feature.

You can also use `iris.feature.optional` to get a define with the feature name `IRIS_FEATURE_X`.

The currently added feature flags are:
`SEPARATE_HARDWARE_SAMPLERS`
`PER_BUFFER_BLENDING`
`COMPUTE_SHADERS`
`ENTITY_TRANSLUCENT`

## Translations

- [Fixed consistency in French translation](https://github.com/IrisShaders/Iris/pull/1668) (Julienraptor01)
- [Updated Korean translation](https://github.com/IrisShaders/Iris/pull/1688) (xphere07)
- [Updated Traditional Chinese translation](https://github.com/IrisShaders/Iris/pull/1676) (HJ-zhtw)
- [Updated Russian translation](https://github.com/IrisShaders/Iris/pull/1677) (Felix14-v2)
