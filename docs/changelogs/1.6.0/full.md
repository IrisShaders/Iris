# Iris 1.6 Changelog (full)

Iris 1.6 has released for 1.18.2, 1.19.2, and 1.19.4.

This release was developed by IMS.

## Overview

- Added `IS_IRIS` define to use the following features
- Added SSBO's
- Added 8 configurable custom read/write images
- Added a new composite pass and a one-time setup compute pass (begin and setup)
- Added optional hybrid-deferred entity batching and 3 new programs for facilitating it
- Added a system for detecting items and armor in the world during rendering
- Added support for 6 more shadow color buffers (shadowcolor2-7)
- Added support for the core specification from OptiFine
- Changed how shadow hardware filtering works

## Bug fixes and mod compatibility improvements

- Added support for FSB's sky path rotation
- Fix bugs with CIT Resewn
- Fix many issues with the world height uniforms
- Do not apply modelview bobbing with shaders off
- Added a held light color API
- Added 2 custom entity ID's (zombie_villager_converting and entity_shadow)

# Feature Details

## SSBO's

Shader storage buffer objects (SSBO's) are a way of passing arbitrary data between programs, and even frames if desired.
They can store any buffer data that fits within the size of the SSBO, and be read at any point, however it is up to the developer to maintain coherency.

How to use:

shaders.properties:

```
bufferObject.index = byteSize
```

Shader program:
```glsl
layout(std430, binding = index) buffer ssbo_name {
    // Any data can go here as long as it's within the size, this is a realistic example.
    // SSBO's also accept unsized arrays, but they must be the last member of the struct.
    int data1;
    vec2 randomData2;
} ssboHandle;

void main() {
    ssboHandle.data1 = 0;
    gl_FragData[0] = vec4(ssboHandle.randomData2, 0, 1);
}
```

## Custom Images

Custom images are a way of passing arbitrary images between programs, and even frames if desired.
They can be read and written to at any point, similar to an SSBO, although similarly you must maintain coherency.

They can either be relative to the screen size or an absolute size, and can be 1D, 2D, or 3D.

How to use:

shaders.properties:
```
image.imageName = samplerName format internalFormat pixelType <shouldClearOnNewFrame> <isRelative> <relativeX/absoluteX> <relativeY/absoluteY> <absoluteZ>
```

Program 1:

```glsl
uniform image2D imageName;
void main() {
    imageStore(imageName, ivec2(gl_FragCoord.xy), vec4(gl_FragCoord.xy, 0, 1));
}
```

Program 2:

```glsl
uniform sampler2D samplerName;

// The difference between a sampler and an image is that you cannot write to a sampler, but it will be filtered linearly.
// Choose what you need depending on the case at hand.

void main() {
    gl_FragData[0] = texture2D(samplerName, gl_FragCoord.xy);
}
```

## Begin/Setup pass

The begin pass is a new composite pass that runs before the shadow pass, and is intended to be used for setting up any data that is needed for the shadow pass.
It can be used as a normal composite.

The `setup` pass is completely new. It can only be used as a compute, and is only run once, during the pack load or when the screen size changes.
However, you can use `a_z` for setup.

## Hybrid Deferred Entity Batching

This is a new feature that allows for entities and other objects that are translucent to be rendered after the deferred pass.

This comes with many implications that will not be detailed here, but to make this possible 4 new programs have been added:
`gbuffers_entities_translucent`, `gbuffers_block_translucent`, `gbuffers_particles`, and `gbuffers_particles_translucent`.

To enable this functionality, add `separateEntityDraws = true` to shaders.properties.

## Item and Armor Detection

A new uniform has been added, `currentRenderedItemId`. This is transformed by Iris at runtime, and is an integer corresponding to the current item/piece of armor being rendered, if it exists.

This is set using `item.properties`. There is also a custom identifier in the form of `trim_material` to detect armor trims. (Example: `trim_emerald`)

## Extra `shadowcolor` buffers

Extra shadowcolor buffers have been added. To use them, activate the feature flag `HIGHER_SHADOWCOLOR`.

# Translations

- [Update Taiwan Chinese translation](https://github.com/IrisShaders/Iris/pull/1905) (notlin4)
- [Add new translations and improve translation consistency in Polish](https://github.com/IrisShaders/Iris/pull/1883) (bascioTOja)
- [Updating Brazilian Portuguese translations](https://github.com/IrisShaders/Iris/pull/1933) (Draacoun)
- [Updating Korean translations](https://github.com/IrisShaders/Iris/pull/1934) (xphere07)
