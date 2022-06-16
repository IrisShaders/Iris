# Notably incompatible mods / mod features that cannot be fixed on the side of Iris

This is a list of issues that, after careful investigation by Iris developers, have been determined to be unfixable on the side of Iris. Please don't open issues regarding them unless you have additional information.

## Botania's custom shaders

These shaders conflict with shaders provided by Iris shader packs. See [the core shaders compatibility document](../development/compatibility/core-shaders.md). This will need to be worked around on Botania's side by not using a custom shader, Iris cannot fix this. 

See https://github.com/VazkiiMods/Botania/issues/3912


## ComputerCraft's TBO-based monitor rendering 

Similarly to Botania, ComputerCraft uses custom shader programs for high-performance monitor rendering. Unfortunately, this doesn't work with Iris shader programs.

The ComputerCraft: Restitched developers have implemented specific workarounds when using shader packs with OptiFine, but have not done so for Iris.

See: https://github.com/cc-tweaked/cc-restitched/issues/1


## Colored Lights by Gegy

Beyond potential issues with shader packs, [Colored Lights isn't compatible with Sodium](https://github.com/Gegy/colored-lights/issues/11), and Iris requires Sodium, so Colored Lights cannot be used with Iris.

See https://github.com/Gegy/colored-lights/issues/11


## Issues when connecting to 1.8-1.12 servers using the Multiconnect mod

Multiconnect completely replaces the content of Minecraft's registries with pre-flattening block IDs when connecting to pre-1.13 servers. Since Iris shader packs use block IDs to determine block effects, and do not expect pre-1.13 block IDs when running on a post-1.13 Minecraft version, this breaks shader packs.

This notably results in solid grass blocks waving like grass plants, since in 1.12 `minecraft:grass` refers to solid grass blocks and in 1.13 `minecraft:grass` refers to the non-solid plant.

We can't fix this on our side, Multiconnect will ideally need to be changed to not disrupt registry content to such an extent.

See https://github.com/Earthcomputer/multiconnect/issues/205


## Immersive Portals: Underwater mirrors are not visible from above water

This is a fundamental limitation of how portals are rendered when shaders are enabled. They are composited on to the scene after rendering has completed, meaning that they cannot be rendered behind translucent surfaces like water.

This cannot be fixed by either Immersive Portals or Iris without completely reworking how portal rendering works with shaders.


# Tweakeroo: Freecam

Tweakeroo's freecam has various issues with Iris. We recommend not using Tweakeroo's freecam feature, and using https://github.com/hashalite/Freecam for freecam instead.

See: https://github.com/maruohon/tweakeroo/issues/301
