# Iris 1.4.3 Changelog (full)

Iris 1.4.3 is now available for download for 1.16.5, 1.18.2, 1.19.2, and very soon 1.19.3, fixing multiple issues and improving stability.
This release also adds some debug features.

This release was developed by coderbot and IMS.

## Features

- Updated to 1.19.3. (IMS)

- New debug screen accessible when a shader compile fails. (IMS)
  - Debug mode is accessible with CTRL-D in the shaderpack selection screen.
  - This is a 1.19.3 only feature.
- Added `shadow.enabled` property for force-enabling/disabling shadows. (IMS)
- Added a warning when using Not Enough Crashes. (IMS)
- Added some basic world info uniforms. (IMS)
  - integer: bedrockLevel, heightLimit
  - float: ambientLight
  - bool: hasCeiling, hasSkyLight

## Bug fixes and mod compatibility improvements

- Fixed multiple depth of field shader issues. (IMS)
  - Fixed NaN values accidentally mixing.
  - Fixed GL3 R32 codepath on 1.16.5.
- Fixed normals in the shadow pass. (IMS)
- Added workaround for waving water disconnecting in Sildur's Vibrant. (coderbot)
- Workaround incorrect lightmap scaling in Continuum 2.0.5. (coderbot)
- Check for linear filtering on integer textures. (IMS & coderbot)
- Temporary fix: Always mark advanced frustum bounds as intersecting.
  - This reduces performance, but prevents a bug that would skyrocket chunk count therefore reducing performance even more.
- Improve robustness of IdMapUniforms. (coderbot)
- Do not modify normal matrix in view bobbing. (IMS)
- Apply eye translucency fix only with shaders enabled. (maximum)
- Don't apply compatibility patches to `gl_` prefixes. (douira)

## Translations

- [Updated German translation](https://github.com/IrisShaders/Iris/pull/1701) (isuewo)
- [Updated Japanese translation](https://github.com/IrisShaders/Iris/pull/1704) (KabanFriends)
- [Updated Portuguese translation](https://github.com/IrisShaders/Iris/pull/1720) (FITFC)
- [Updated Chinese translation](https://github.com/IrisShaders/Iris/pull/1734) (GodGun968)
- [Updated Taiwanese translation](https://github.com/IrisShaders/Iris/pull/1754) (HJ-zhtw)
- [Updated Russian translation](https://github.com/IrisShaders/Iris/pull/1741) (Felix14-v2)
- [Updated Ukrainian translation](https://github.com/IrisShaders/Iris/pull/1742) (Altegar)
