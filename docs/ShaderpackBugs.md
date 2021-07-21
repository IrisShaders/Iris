# Shaderpack Bugs

This file tracks some bugs in shader packs that might appear to be Iris issues, but are just bugs within the shader pack.

## SEUS Renewed v1.0.1

* Enchantment glints don't render
    * Diagnosis: This is because the TAA jitter is applied to entities, but not to enchantment glints. The enchantment
      glint must be transformed in the exact same way as the entity it is being applied to or else it will break.
    * Workaround: Replace `TemporalJitterProjPos(gl_Position);` with `// TemporalJitterProjPos(gl_Position);` on line 116 of gbuffers_entities.vsh
    * Confirmation: This bug has been confirmed to be observable in both Iris and OptiFine.

## Sildur's Vibrant Shaders v1.29

* Enchantment glints have z-fighting in the nether and end
    * Diagnosis: In the nether and end, `gbuffers_armor_glint` uses a series of matrix multiplications, but
      `gbuffers_textured` uses ftransform. The matrix multiplications introduce precision errors compared to ftransform
      that cause the vertex depths to be subtly different, which is enough to cause issues with the glint effect. The
      glint must have the exact same transformations as the content it's being applied to in order to work properly,
      otherwise issues like this show up.
    * Fix: Replace the content of lines 27-29 of gbuffers_armor_glint.vsh with `gl_Position = ftransform();`
    * Tracking issue: https://github.com/Sildurs-shaders/sildurs-shaders.github.io/issues/158
