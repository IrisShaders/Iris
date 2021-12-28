# Iris 1.1.4 Preliminary Changelog (Full)

WIP, up-to-date as of:

- 1.16.x: https://github.com/IrisShaders/Iris/commit/64359ef7fb1f367a29c9ca0a85885404cde80de7
- 1.17.x: https://github.com/IrisShaders/Iris/commit/315f3d8de32c970e2298a1c863192c67e5d80ce2
- 1.18.x: https://github.com/IrisShaders/Iris/commit/36be55d6cde8fdf1578c56ca146ed005e80bbd5b

---

This release includes multiple bug fixes, as well as some internal features needed for existing packs to work better.

## Major features/bug fixes on all versions
- Iris now binds to the correct buffer for render targets, fixing TAA on many packs/drivers.
- Iris now correctly shifts the cameraPosition and previousCameraPosition variables.
    - Previously, this was not the case, which caused issues at extreme distances from spawn on many packs.
- Iris now supports the rain.depth, vignette, and underwaterOverlay properties (credit to maximumpower55). 
    - Previously, Iris would ignore requests for these to be toggled.
- Iris now correctly disables blending in the shadow pass by default.
    - Previously, this was not the case, causing weird issues with colored glass in the shadow pass on some packs.
- Iris now correctly supports the `RENDERTARGETS` directive.
    - Previously, Iris would ignore it, causing some packs to be broken/incorrect.

### Minor bug fixes

- Iris now uses the correct pixel format on render targets, fixing a crash with Wisdom Shaders. (Wisdom is still not officially supported)
- Iris no longer assumes a pipeline is non-null in many cases. (Fixes a crash trying to access HD Skins from the main menu)

## 1.18-specific fixes

- Iris now uses the correct getter method for render distance, fixing issues with fog/white blocks in the sky on multiplayer.

## 1.17.1+ specific fixes
- Iris now can recreate vanilla shaders when a shader pack does not provide one to override.
  - Previously, Iris would skip the program, leading to issues with packs like Sildur's Enhanced Default not having block outlines due to a lack of gbuffers_basic.
- Iris now uses the correct vertex normals underwater.
    - Previously, Iris would use incorrect normals, causing weird symmetry in underwater reflections.
- Iris now supports fogDensity simulation on 1.17.
  - Previously, fogDensity would always be 0, causing fog issues underwater on some packs.
- Iris no longer creates a `patched_shaders` folder in non-development environments.
    - Previously, this would take up unnecessary space, as well as causing errors if the folder was read only.

## Internals

- Iris now silences JCPP preprocessing errors.
- Iris now has a more robust initialization, preventing some errors caused by mods interfering with loading.
- Iris now inserts Sodium imports in front of extension directives on 1.17 and higher.
- Iris now has a single patcher file, instead of multiple for composite and Sodium patching.
- Iris makes sure alpha test overrides are not themselves overriden.

## Translations

- TODO
