# Iris 1.2.6 Changelog (full)


Iris 1.2.6 is now available for download, fixing minor bugs and adding support for 1.19.1.

We hope for this to be one of the last big bugfix releases before PBR (Iris 1.3.0). When we release PBR, we plan to drop support for 1.17.1, meaning that the Iris release supporting PBR will only support 1.16.5, 1.18.2, and potentially 1.19 if Mojang releases it by then. We hope this makes sense, because actively supporting 4 different Minecraft versions would just be too much with our current development team.

This release was developed by coderbot, IMS, and Pepper.

## Version support

- Added support for 1.19.1 (IMS)

## Performance

- Avoided allocations in the shadow frustum calculation to improve shadow performance slightly. (IMS)

## Bug fixes and mod compatibility improvements

- Use overworld time in nether/end to match OptiFine behavior (IMS)
- Use a version counter to reload Sodium shaders (qouteall)
- Use lower Mixin priority in MixinClientLanguage to avoid issues with Spectrum 1.4.0 (Justsnoopy30)
- Fixed the entityColor geometry shader passthrough (IMS)
- Modify block outline wrap function to be compatible with Create 0.5 (IMS)
- Preserve GUI render state when vignette is off (Sorenon)
- Fix normals on the inside of flowing water (IMS)

### Iris exclusive features

- Added firstPersonCamera bool uniform (IMS)

### OptiFine parity

- Added at_midBlock support (Pepper)
    - This is a OptiFine attribute that measures the pixel relative to the middle of the block in 1/64 segments.
- Added support for the `shadowInternalFormat`, `shadowClearColor`, `shadowClear` const variables. (IMS)
- Added support for `usampler2D` and `uimage2D` samplers (IMS)
- Added support for 8 and 16 bit unsigned integer formats (IMS)
- Added full support for the `clouds` option (coderbot)
- Added support for the `sun` and `moon` options (coderbot)
- Handle basic disabling of programs (Justsnoopy30)

## Translations

- [Updated Slovakian translation](https://github.com/IrisShaders/Iris/pull/1548) (SmajloSlovakian)
