# Iris 1.7.0 Changelog

## New Features

- Added Tessellation shaders. These new shader types allow you to subdivide and add more detail to geometry.
    - These take the form of `.tcs` and `.tes` shaders respectively for control and evaluation. See the Khronos wiki for
      details.
- Added an attribute that allows you to see the emission value of a block. It is stored in `at_midBlock.w` and ranges
  from 0-15.
- Added `cameraPositionInt` and `cameraPositionFract` for better precision, along with their respective previous
  uniforms. *These values are unshifted, unlike the normal ones.*
- Added `occlusion.culling` option.
- Added support for Distant Horizons 2.0.3. This version is not officially released yet.
- Added debug groups. This groups together information in RenderDoc for easy viewing.
- Added support for Indirect Compute shaders. This allows you to dispatch a compute shader with the work group amount
  specified from a SSBO.
    - To use this, you must use `indirect.pass = bufferObjectNumber offsetInBuffer` in shaders.properties, and the
      object at offset in the
      SSBO must be an `uvec3`.
        - ***If you do not do this, your PC will most likely crash trying to dispatch 2147483647^3 work groups. Don't do
          that.***
- Added a keybind to see a wireframe view of the world. This is only usable in singleplayer.
- Added some uniforms, mostly `isRightHanded`.

## Technical Changes

- Relocated Iris from `net.coderbot` to `net.irisshaders`
    - This will break any mods depending on Iris 1.6.17 and below.
- Added whitespace detection code to property files.
    - This fixes SEUS PTGI GFME.
- Redesigned the pipeline layout.
