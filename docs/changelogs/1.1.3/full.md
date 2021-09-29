# Iris 1.1.3 Preliminary Changelog (Full)

WIP, up-to-date as of:

- 1.16.x: https://github.com/IrisShaders/Iris/commit/12623519a039d62479c65e70e756a0f9307f55af
- 1.17.x: https://github.com/IrisShaders/Iris/commit/2767fbf6d70a177cc33ce1848f724a10cb6d1e8b

## New Shader Features and Fixes

- Witches and drowned now have emissive parts when running with Complementary Shaders
    - Previously, they did not because atlasSize did not properly reflect the size of the currently bound atlas texture
    - Rather, it was always the size of the block atlas texture
    - With this fixed, the emissive features in Complementary's Integrated PBR now works properly

## 1.17-specific fixes

- The bottom face of end portal blocks is now properly positioned when shaders are enabled
    - https://github.com/IrisShaders/Iris/issues/859

## 1.16-specific fixes

- Iris now works around a vanilla 1.16 issue related to a fatal crash bug in certain AMD drivers when calling glShaderSource. This crash only affected fabulous shaders & entity outline shaders.
    - Iris already fixed the glShaderSource crash on all versions for shaders loaded from shader packs.
    - Vanilla fixes this in 1.17+ for all vanilla shaders.
- Origins Enhanced is now properly marked as incompatible on 1.16 only due to it not working with Sodium 0.2.0, which our bundled version of Sodium is based on.

## Internals

- Iris now includes an expression parser written by Kroppeb, a critical part of implementing custom uniforms.
- Iris now uses Mojang Mappings instead of Yarn Mappings. This doesn't change anything for users, but it does streamline our development process & workflow for handling Minecraft updates.
- Iris no longer depends on fabric-api-base and fabric-lifecycle-events, shrinking the released JAR size by around 60 kB.
- Code has been added to support the addition of Iris-exclusive shader features in a way that allows shader authors to detect their presence.
