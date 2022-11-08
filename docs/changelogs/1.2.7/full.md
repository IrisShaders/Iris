# Iris 1.2.7 Changelog (full)

Iris 1.2.7 is now available for download for 1.16.5, 1.17.1, 1.18.2, and 1.19.2, improving performance and fixing minor bugs.

**This will be the last release for 1.17.1.**

This release was developed by coderbot and IMS.

## Updates

- Updated to Sodium 0.4.3 (IMS)

## Performance

- Rewrote shadow frustum calculation to improve shadow performance, and allow skipping some checks on 1.17+. (IMS)

## Bug fixes and mod compatibility improvements

- Added custom lighting API (parzivail)
- Pass through vertex alpha in AO separation (parzivail)
- Switch to using renderTargets to avoid `centerDepthSmooth` breaking on reload (IMS)
- Many UI fixes (coderbot)
    - Don't allow import/exports while fullscreened to avoid "breaking" the window
    - Do not color Off/On when it's the default option
    - Apply the new pack if you switch between a pack and shader config
    - Allow configuring shader packs in fallback mode
    - If a main screen is not defined, add all options to it by default
    - Sort shader packs list alphabetically, and ignore color codes
- Work around shader bug causing white entities (coderbot)
- Map water blocks to still instead of flowing water in LegacyIdMap (coderbot)
- Allow shader programs to not write to any color buffers (coderbot)


### Iris exclusive features

- Added `currentPlayerHealth` (0-1) and `maxPlayerHealth` (default 20) uniforms (IMS)
- Added `currentPlayerHunger` (0-1) and `maxPlayerHunger` (default 20) uniforms (IMS)
- Added `currentPlayerAir` (0-1) and `maxPlayerAir` (default 300) uniforms (IMS)
- Added `eyePosition` uniform (IMS)
- Added `isSpectator` uniform (coderbot)

## Translations

- [Updated Russian translation](https://github.com/IrisShaders/Iris/pull/1592) (Felix14-v2)
- [Updated Mandarin Chinese translation](https://github.com/IrisShaders/Iris/pull/1581) (HJ-zhtw)
- [Renamed Ukranian translation to function properly](https://github.com/IrisShaders/Iris/pull/1594) (Borusu1)
