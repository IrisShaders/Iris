# Shaderpack Bugs

This file tracks some bugs in shader packs that might appear to be Iris issues, but are just bugs within the shader pack.

## All shader packs

* Vanilla shadows appear in addition to dynamic shadows
    * Diagnosis: Shader packs force there to be shadows where there is low skylight to prevent light from leaking into caves. [While I would like to make this workaround unnecessary](https://github.com/IrisShaders/Iris/issues/317), it is entirely something implemented in the shaderpack, Iris doesn't really have control over it.
    * Workaround: no general workaround, each pack implements this differently.
* Translucency issues
    - Examples:
        - Water renders wrongly through beacon beams
        - World border interacts oddly with water
        - Transparent nametags when looking at someone sneaking in front of water
    - Diagnosis: Deferred rendering / translucency sorting issues inherent to many shader packs

## SEUS Renewed v1.0.1

* Enchantment glints don't render
    * Diagnosis: This is because the TAA jitter is applied to entities, but not to enchantment glints. The enchantment
      glint must be transformed in the exact same way as the entity it is being applied to or else it will break.
    * Workaround: Replace `TemporalJitterProjPos(gl_Position);` with `// TemporalJitterProjPos(gl_Position);` on line 116 of gbuffers_entities.vsh
    * Confirmation: This bug has been confirmed to be observable in both Iris and OptiFine.

## Sildur's Vibrant Shaders v1.29

* Weird black spots and red/green/yellow lines on Sildur's Vibrant at very specific large window resolutions
  (windowed mode on a 3440x1440 monitor for example)
    * Diagnosis: Not attempted
* Enchantment glints have z-fighting in the nether and end
    * Diagnosis: In the nether and end, `gbuffers_armor_glint` uses a series of matrix multiplications, but
      `gbuffers_textured` uses ftransform. The matrix multiplications introduce precision errors compared to ftransform
      that cause the vertex depths to be subtly different, which is enough to cause issues with the glint effect. The
      glint must have the exact same transformations as the content it's being applied to in order to work properly,
      otherwise issues like this show up.
    * Fix: Replace the content of lines 27-29 of gbuffers_armor_glint.vsh with `gl_Position = ftransform();`
    * Tracking issue: https://github.com/Sildurs-shaders/sildurs-shaders.github.io/issues/158
* Block breaking animations have weird colors at certain camera angles, and don't show up on chests / other block entities.
    * Diagnosis: No gbuffers_damagedblock program is provided, meaning that gbuffers_terrain is used for rendering block
      breaking animations. This program is not set up to handle translucency, causing the weird colorations due to it
      blending oddly with existing terrain data. Furthermore, since the deferred rendering code handles terrain separately
      from block entities, the block breaking animation is written to the terrain render target instead of being blended
      with the block entity.
    * Workaround: unknown.
- Sky is visible through chests if a nether portal is in front of the chest
    - Diagnosis: The chest pixels get blended with the nether portal pixels and then the combined pixels get blended with the horizon with the nether portal's alpha value
    - Workaround: none, inherent issue with how Sildur's Vibrant does deferred rendering.

## Chocapic v4

* Random flashing holes in the terrain
    * Diagnosis: Some waving parameters are left uninitialized instead of being initialized to zero, causing some
      non-waving blocks to get undefined vertex positions. This appears to be caused by a failed attempt to initialize
      all parameters to zero using only a single assignment.
    * Workaround: Apply the following diff:
      ```diff
      -	float parm0,parm1,parm2,parm3,parm4,parm5 = 0.0;
      +	float parm0 = 0.0,parm1 = 0.0,parm2 = 0.0,parm3 = 0.0,parm4 = 0.0,parm5 = 0.0;
      ```
