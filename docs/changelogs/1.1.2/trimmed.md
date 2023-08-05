# Iris 1.1.2 Changelog (Trimmed)

Iris version 1.1.2 for Minecraft 1.16.5, Minecraft 1.17.1, and 1.18 snapshots (see below) has been released!

You can download the release here: https://irisshaders.dev/download.html

A trimmed changelog is available below. A more detailed changelog is available [on GitHub](https://github.com/IrisShaders/Iris/blob/trunk/docs/changelogs/1.1.2/full.md) for those who are interested.

## Notable Changes

- This release includes a new shadow culling system as well as a new batched entity rendering system, improving performance with many shader packs and fixing a bunch of bugs!
- For 1.17.1 and above, this release also contains a modified version of Sodium based on 0.3.2 instead of 0.3.0 like before, improving performance and graphics driver compatibility.

## Major changes for all versions

- Shadow culling has been completely rewritten. It no longer incorrectly culls chunks compared to OptiFine, and overall performance has improved greatly compared to the previous system (especially with shader packs like Enhanced Default, BSL, and Complementary Shaders).
- A Max Shadow Distance slider is now available in Video Settings, allowing you to tweak the render distance of shadows on packs that don't specify one.
- Batched entity rendering has been completely rewritten, fixing many memory management, correctness, and performance issues.
- Fog now fully works in most shader packs, including Sildur's Enhanced Default and other vanilla-like shaders
- Entities now flash red when hurt, and flash white when they are going to explode
- End portals now properly render with most shader packs
- blockEntityId and entityId are now fully supported (Fixes many issues with Complementary Shaders' Integrated PBR)
- Iris now has initial support for the path-traced lighting in SEUS PTGI, and has some fixes to the lighting in SEUS Renewed. (SEUS PTGI is still not officially supported, its water still does not work on Iris.)

## Major fixes for 1.17.1 and above

- End gateway beams no longer appear to render twice with shader packs that use shadows.
- Fixed separate leads incorrectly rendering as connected
- Block selection outlines now render properly, even with world curvature enabled. (due to a different issue that will be fixed soon, this doesn't work with some packs like Enhanced Default.)
- Chunk borders (F3 + G), hitboxes, and other lines now render properly with shaders enabled. (similarly, this doesn't work with some packs like Enhanced Default.)
- World borders render properly with most packs now.
- The energy swirl around charged creepers now renders properly.

## Mod compatibility fixes

- Fix most issues with Physics Mod
- Initial compatibility with Immersive Portals is now available on 1.17.1, though there are still issues. (credit to qouteall for implementing the compatibility code)
- Fixed an issue where directional shading would be enabled on some blocks rendered with the Fabric Rendering API, even if the shader pack disabled it. (Fixes issues with CTM mods)
- Added a new screen for when you have an incompatible version of Sodium installed.

## But wait, there's more!

For the first time, we are releasing versions of Iris for the 1.18 snapshots for everyone to use. These builds do not represent the final version, however, they should be mostly playable (though there are significant performance issues caused by 1.18 itself.) These builds will be released to Patrons first for testing, then released for the general public after a few days once we confirm no major issues are in them. Currently, you can only get 1.18 versions of Iris through Github Releases (for Fabric). Overall, shaders should be mostly usable on 1.18 snapshots!
