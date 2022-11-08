# Iris 1.2.5 Changelog (full)


Iris 1.2.5 is now available for download, fixing a huge number of bugs, massively improving performance with packs using depth of field, and improving performance in some CPU-limited scenarios.

We hope for this to be one of the last big bugfix releases before PBR (Iris 1.3.0). When we release PBR, we plan to drop support for 1.17.1, meaning that the Iris release supporting PBR will only support 1.16.5, 1.18.2, and potentially 1.19 if Mojang releases it by then. We hope this makes sense, because actively supporting 4 different Minecraft versions would just be too much with our current development team.

This release was developed by coderbot & IMS with additional help from Pepper.

## Features

- Added update checker (IMS)
    - The Iris version is now displayed in the lower left corner of the shader pack selection screen.
    - The update checker can be disabled in iris.properties for people making modpacks or using their own mod updater.
    - We added this because we kept on discovering people using really ancient versions of Iris and missing out on bug fixes and even features like shader configuration.

- Added a text sink API
    - This is needed to render text in-world with shaders.


## Performance

- Fixed the first world load taking more time than it should (coderbot)
- Fixed an unnecessary freeze when closing the shader pack GUI even if not switching packs or changing settings (coderbot)
- Implemented some optimizations increasing FPS by as much as 20%-40% in some CPU limited situations (coderbot)
    - Avoided redundant glBindFramebuffer and glUseProgram calls
    - Avoided an oversight causing vanilla clouds to be rebuilt every frame caused by shadow map rendering
    - The extended vertex format is now disabled for most things outside of world rendering
- Implemented a massive speedup for depth-of-field effects, DoF now has a near-zero performance impact (IMS)
- Optimized texture atlas stitching to reduce VRAM usage in some cases, even with shaders (Pepper)
- Iris now fully supports mods using the stencil buffer without causing performance drops or log spam (coderbot)


## Bug fixes and mod compatibility improvements

- Fixed another source of major chunk loading corruption related to shader reloading (coderbot)
- Fixed lighting and POM issues with sign text and handheld maps on some shader packs (coderbot)
- Fixed some issues with triangle meshes when using the extended vertex format (Pepper)
- Fixed issues with the dragon death animation on all versions (coderbot & IMS)
- Fixed cameraPosition resetting every 1000 blocks, now it resets only every 30000 blocks. This caused a "jump" in water and foliage movement periodically when travelling long distances (IMS)
- Ensured that shaders attempting to bind the lightmap as a custom texture work properly (coderbot)
- Fixed lighting breaking with shader packs specifying an out-of-range ambientOcclusionLevel value (coderbot)
- Fixed a number of issues with preprocessing of .properties files (coderbot)
- Fixed vertex tangent vectors in some cases, fixes stone appearing black on SEUS PTGI HRR 3 and some other issues (Pepper)
- Fixed an issue where OpenGL debugging would be enabled by default, which caused significant log spam in some cases (IMS)
- Improved the robustness of shader skybox rendering, fixes some massive brokenness with sky rendering in custom dimensions or with custom skyboxes (coderbot)
    - Iris also no longer renders the shader skybox with shaders disabled, fixes an issue with Distant Horizons
- Fixed small issues with First Person Model entity shadows (coderbot)
- Removed IrisRenderLayerWrapper on all versions, fixes compatibility with some mods like Arcanus and Requiem
- Fixed enchantment glints on Enhanced Default, world borders on many packs, lightsabers from Parzivail's star wars mod, etc (coderbot)
    - Default samplers and vertex attribute data is now provided for all programs if they are missing.
- Added compatibility for Lifts monitors (coderbot, IMS)
- Added some basic compatibility for Litematica (coderbot)
- Fixed rendering issues with Light Overlay (coderbot)
- Commands have been removed due to issues with the Fabric Maven (IMS)
- Use floats for midTexCoord in terrain XHFP (Pepper)

## Newly-compatible shader packs

- Added hardcoded support for Super Duper Vanilla Shaders (IMS)
- Fixed some major issues with Chocapic v4, Chocapic v5, and Triliton's Shaders (coderbot)
    - Chocapic v7, v8, and v9 are still not supported


## New shader developer features

### Iris exclusive features

- Added thunderStrength Iris-exclusive uniform (IMS)
- Added shadowPlayer Iris-exclusive shaders.properties option, allows rendering the player and vehicles the player is riding without rendering any other entities (IMS)
    - This is intended for lightweight shader packs that don't want to take on the performance hit of rendering shadows for all entities.


### OptiFine parity

- Added support for wetnessHalfLife, drynessHalfLife, and eyeBrightnessHalfLife (IMS)
- Iris now allows shader packs to change the render type of blocks (IMS)



## 1.16-specific fixes:

- Completely refactored Iris program matching on 1.16.x, fixing a massive number of 1.16.x mod compatibility issues (coderbot)
    - Iris for 1.16.x is now able to automatically figure out the right program to use based on the OpenGL state in almost all cases.
- Fixed StackOverflowError when loading Complementary Shaders (coderbot)
- Fixed massive rendering issues with mods not using vanilla built-in render types (Artifacts, AdventureZ)
- Fixed rendering issues with Campanion tent previews
- Fixed compatibility with Orderly


## 1.17 and 1.18 specific fixes:

- Fixed white dots appearing on the screen and other weirdness (IMS)
    - This was caused by non-overridden shader programs writing to the depth buffer
- Fixed slime blocks disappearing while being moved by pistons (IMS)
- Fixed enderman eyes not being colored (coderbot)
- Fixed an issue when using RenderDoc (coderbot)
- Fixed an issue where shaders would not apply on old nvidia drivers on 1.17+ (IMS)
- Fixed an issue where shaders would not apply on Intel Windows drivers for HD 4000, HD 2500, and similar cards on 1.17+ (coderbot)
- Fixed the in-game profiler breaking when using a shader pack with a shadow pass


## Translations

- [Added a missing comma to the Russian translation](https://github.com/IrisShaders/Iris/pull/1426) (Disketaa)
- [Updated Chinese translation](https://github.com/IrisShaders/Iris/pull/1348) (Crsuh2er0)
- [Updated Taiwanese translation](https://github.com/IrisShaders/Iris/pull/1384) (HJ-zhtw)
