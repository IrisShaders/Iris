# Iris 1.6.5 Changelog

- Fixed NEC/Sodium screen bypass not working
- Moved the "Color Space" toggle to Quality options.
- Added `entity_flame` special ID.
- Fixed a crash with core profile shaders.
- Fix mismatched push/pop when in spectator on some packs.
- Added a fake vertex shader subsystem, to fix *extremely* old packs without vertex shaders.
- Fixed fake entity shadows on some packs.
- Fixed lightning bolts not taking on the correct entityId.
- Enabled smooth triangle (not quad) shading.
- Added `dimension.properties`.
- Added `is_on_ground`.
- Added support for armor trim detection.

### dimension.properties

When `dimension.properties` is added to the shader pack root, behavior relating to dimensions change.
Iris will no longer resolve any dimensions for you, and you are expected to resolve you own.

The syntax for dimension.properties is as follows:

`dimension.folderName = dimensionNamespace:dimensionPath`

*You can use `*` as a value to fallback all dimensions to.*
