# Iris 1.1.3 Changelog (Full)

This release includes significant changes to the project organization of Iris, as well as fixes for some rendering bugs in the shader packs supported by Iris, especially when using manually-edited shader configuration.

This release also marks the completion of the preparatory work needed for Iris to properly support shader configuration. We intend to focus our work on supporting shader configuration in the near future, but we have no ETA for when this will be available.

## Sodium

- Iris no longer bundles Sodium, you must now download Sodium separately.
    - Iris no longer requires a forked version of Sodium to function properly, and uses Mixins to work with official Sodium.
    - Sodium is still automatically installed in the installer
    - This change is being made in order to reduce confusion between Iris and Sodium as projects, and to streamline the development of Iris.

## New Shader Features and Fixes

- The player hand / hands are now properly lit and shaded with all shader packs (credit to maximumpower55 and IMS)
    - Previously, they were rendered outside of the world without shaders, which could severely break immersion, especially with packs that implemented handheld dynamic lighting.
- Witches and drowned now have emissive parts when running with Complementary Shaders
    - Previously, they did not because atlasSize did not properly reflect the size of the currently bound atlas texture
    - Rather, it was always the size of the block atlas texture
    - With this fixed, the emissive features in Complementary's Integrated PBR now works properly
- Scene-Aware Colored Lighting is now supported in Complementary Shaders, if you enable it manually
    - Iris now supports 16 color buffers instead of 8, just as OptiFine G8 and newer do.
    - For shader developers: colortex8+ is usable on macOS on Iris (but not OptiFine), as long as a single shader program doesn't use more than 16 samplers
- Rainbows, rain puddles, and aurora borealis effects now work with Complementary Shaders (credit to IMS for the fix)
    - Iris now hardcodes support for the required custom uniforms similarly to the hardcoded support it has for other custom uniforms
    - In the future, Iris will properly support custom uniforms, and hardcoding will not be needed.
- Fixed fullscreen passes using the wrong quad coordinates (credit to Niemand / Kneemund for the contribution)
    - This severely broke the fullscreen passes of some shaders that Iris doesn't yet support but can support in future updates.
- Fixed loading "Apex Shaders v1.4" causing a game crash when launching.
- Fixed rain causing graphical corruption on SEUS Renewed
    - Custom texture support was required
- Fixed water and rain looking completely broken on SEUS PTGI and SEUS PTGI HRR
    - Support for both custom textures as well as blend mode overrides was required
- Fixed stairs emitting light on SEUS PTGI HRR
    - JCPP is now used to preprocess block ID maps, previous versions of Iris processed them incorrectly causing this issue.
- Iris supports manually enabling the Galaxies setting of Complementary Shaders
    - Custom texture support was required
- Iris now supports the planets in the AstraLex night sky
    - You guessed it, it was custom textures
- Fixed bugs from manually changing Specular Reflections / RP Support on Complementary / BSL
    - JCPP is now used to properly conditionally parse directives, instead of using hardcoded workarounds that didn't support changed settings.
- SEUS should now work properly on Mesa drivers without additional external configuration (such as )
    - Iris now hoists active `#extension` directives to the top of the shader file to adhere strictly to the GL spec, allowing these shaders to compile.
- Fixed fog looking incorrect in the nether with Enhanced Default

## 1.17.1+ specific fixes

- The bottom face of end portal blocks is now properly positioned when shaders are enabled
    - https://github.com/IrisShaders/Iris/issues/859

## 1.16-specific fixes

- Iris now works around a vanilla 1.16 issue related to a fatal crash bug in certain AMD drivers when calling glShaderSource. This crash only affected fabulous shaders & entity outline shaders.
    - Iris already fixed the glShaderSource crash on all versions for shaders loaded from shader packs.
    - Vanilla fixes this in 1.17+ for all vanilla shaders.
- Origins Enhanced is now properly marked as incompatible on 1.16 only due to it not working with Sodium 0.2.0, which our bundled version of Sodium is based on.
- Use the correct far plane for fog, avoiding some issues with things in the world clipping off at far distances.

## Internals

- Iris now includes an expression parser written by Kroppeb, a critical part of implementing custom uniforms.
- Iris now uses Mojang Mappings instead of Yarn Mappings. This doesn't change anything for users, but it does streamline our development process & workflow for handling Minecraft updates.
- Iris no longer depends on fabric-api-base and fabric-lifecycle-events, shrinking the released JAR size by around 60 kB.
- Code has been added to support the addition of Iris-exclusive shader features in a way that allows shader authors to detect their presence.

## Translations

- Added lolcat translation (credit chalkyjeans)
- Added Pirate Speak translation (credit NoComment, IMS)
- Added Slovak translation (credit SmajloSlovakian)
- Updated Estonian translation (credit Madis0)
- Updated Farsi translation (credit alirezaahani)
- Updated German translation (credit GameWithJerry, Levelowel)
- Updated Italian translation (credit Dar9586)
- Updated Persian translation (credit alirezaahani)
- Updated Portuguese translation (credit Maneschy12)
- Updated Russian translation (credit Disguys, BratishkaErik)
- Updated Traditional Chinese translation (credit HJ-zhtw, Canary233)
- Updated Korean translation (credit craftingmod)
