# Iris 1.1.4 Changelog (Full)

This release mainly focuses on bug fixes, and closes over 50 GitHub issues! It resolves a huge number of bugs, and the Iris experience with supported packs should be substantially improved as a result. Thanks to the many contributors that made this release possible!

Shader configuration is also in the final testing & bug fixing phases, meaning that very soon official Iris releases will support modifying shaders through a GUI without the need for any 3rd-party forks or workarounds. Stay tuned for more updates on that!

## Major fixes

- Iris has much more robust handling of incompatible Sodium versions, ensuring that an intuitive error dialog is shown in all cases (coderbot, IMS).
    - Previously, the mixins into Sodium classes were not disabled when an incompatible Sodium version was present
    - In addition, some incompatible versions of Sodium passed the version check (notably, the 1.18+ dev builds), leading to confusing crashes.
- Iris no longer uses the extended vertex when shaders are disabled, avoiding performance loss with shaders disabled compared to base Sodium (IMS).
- Fixed TAA issues on many packs / drivers caused by internal confusion between GL_READ_BUFFER and GL_READ_FRAMEBUFFER (coderbot).
  - Sildur's Vibrant Shaders now works great on Mesa drivers with Iris as a result, and Oceano now works on Iris without terrible vibrating as well.

## Other issue fixes on all versions

- Iris now correctly shifts the cameraPosition and previousCameraPosition variables to avoid precision issues.
    - Previously, this was not the case, which caused issues at extreme distances from spawn on many packs.
- `shaders.properties`: Iris now supports the `rain.depth`, `vignette`, and `underwaterOverlay` properties (credit to maximum#8760) as well as the `shadow.culling` and `particles.before.deferred` properties (IMS). 
    - Previously, Iris would ignore requests for these to be toggled.
- Iris now correctly disables blending in the shadow pass by default (coderbot, IMS)
    - Previously, this was not the case, causing weird issues with colored glass in the shadow pass on some packs.
- Iris now correctly supports the `RENDERTARGETS` directive used by more modern shader packs (credit to Justsnoopy30)
    - Previously, Iris would ignore it, causing some packs to be broken/incorrect.
- Fixed a failure during shader pack loading if a custom texture mcmeta file didn't contain both `blur` and `clamp`, needed by packs like Spectrum (credit to Justsnoopy30)
- Iris now uses the correct pixel format on render targets, fixing a crash with packs like Wisdom Shaders. (Wisdom Shaders still does not work on Iris)
- Added initial support for image load/store for colorimg0 through colorimg15 (compared to only colorimg0 through colorimg5 on OptiFine) and shadowcolorimg0/1, [though memory barriers are currently unimplemented](https://github.com/IrisShaders/Iris/issues/1089) which could lead to inconsistent behavior (credit to BruceKnowsHow)
- Bumped the number of potential composite/deferred programs up to 99
- Added support for the `blendMode` uniform (IMS)
- Fixed some log spam during the preprocessing of .properties files (credit to Justsnoopy30)
- Fixed entity shadows not appearing on packs without shadowmap shadows (IMS)
- Fixed Max Shadow Distance resetting after each boot (NoComment1105)
- Work around a vanilla crash due to OptiFine setting maxFps to 0 in options.txt in some cases.

## 1.16-specific fixes

- Iris makes sure shaders.properties alpha test overrides are not themselves overridden on 1.16 (credit to maximum#8760).

## 1.17.1+ specific fixes

- Fixed various world elements not rendering with packs like Enhanced Default, Waving Plants v3.5, Stereo's Default+, etc that do not override all shader programs (coderbot).
  - Previously, Iris would skip the program, leading to issues with packs like Sildur's Enhanced Default not having block outlines due to a lack of gbuffers_basic.
  - Iris now can recreate vanilla shaders when a shader pack does not provide one to override. It overrides shaders from vanilla / the resource pack to support things like exponential fog & avoid loading modified core shaders.
- Fixed broken underwater reflections on many packs (coderbot).
    - Previously, Iris would use incorrect normals, causing weird symmetry in underwater reflections.
- Iris now supports exponential water fog on 1.17+ for shader packs, fixing some potential issues with vanilla+ shader packs (coderbot)
  - Previously, fogDensity would always be 0, causing fog issues underwater on some packs.
- Iris no longer creates a `patched_shaders` folder in non-development environments (coderbot).
    - Previously, this would take up unnecessary space, as well as causing errors if the folder was read only.
- Updated fog far plane code to match OptiFine (IMS)
- Iris now inserts Sodium imports in front of extension directives on 1.17 and higher, avoiding shader compile errors / crashes (IMS)

## 1.18-specific fixes

- Iris now uses the correct getter method for render distance, fixing issues with fog/white blocks in the sky on multiplayer (IMS).

## Mod Compatibility fixes

- Added a more robust initialization sequence to avoid weird crashes happening when Not Enough Crashes catches a crash by a different mod during initialization (coderbot)
- Fixed compatibility with Origins' night vision buff as well as similar night vision features in other mods (credit to Mourdraug).
- Fixed a crash trying to access HD Skins from the main menu that was caused by Iris assuming that entities could only be rendered while in a world (coderbot)
- Added an initial stable Iris API so that other mods can query some Iris state & open the Iris config GUI without making unsupported calls into internal Iris code (coderbot)
- Iris now uses RenderSystem/GlStateManager where possible in addition to putting other direct GL calls in IrisRenderSystem, hopefully helping with mods like Blaze4D that try to replace Minecraft's graphics library (IMS)

## Notable internal changes / refactors

- Iris now has a single patcher file, instead of multiple for composite and Sodium patching (IMS)
- Check for duplicated uniforms to prevent confusing internal errors (credit to maximum#8760).
- Made progress towards fixing the shaderpack test suite breaking due to direct / indirect MC references (coderbot)
- Fix syntax error in build.gradle (credit to douira)
- Added an automated release system (credit NoComment1105)

## Translations

- Added Classical Chinese (lzh) translation (credit Hulkenius)
- Added Czech translation (credit Fjuro)
- Updated Chinese translation (credit klkq)
- Updated Taiwanese translation (credit HJ-zhtw)
- Updated Polish translation (credit SSajda)
- Fixed Korean translations using the wrong file (credit craftingmod)
