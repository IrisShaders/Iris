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


## Immersive Portals: Underwater mirrors are not visible from above water

This is a fundamental limitation of how portals are rendered when shaders are enabled. They are composited on to the scene after rendering has completed, meaning that they cannot be rendered behind translucent surfaces like water.

This cannot be fixed by either Immersive Portals or Iris without completely reworking how portal rendering works with shaders.


# Tweakeroo: Freecam

Tweakeroo's freecam has various issues with Iris. We recommend not using Tweakeroo's freecam feature, and using https://github.com/hashalite/Freecam for freecam instead.

See: https://github.com/maruohon/tweakeroo/issues/301
