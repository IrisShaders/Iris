# Iris 1.2.0 Changelog

Iris 1.2.0 is here for Minecraft 1.16.5 / 1.17.1 / 1.18.1, bringing the long awaited Shader Configuration GUI!

The shader configuration system and GUI was a massive undertaking, with development of this iteration beginning in June 2021 with development of the previous iteration (as seen in Starline) having started in February 2021.

While shader config might seem like a simple enough request, the reality is that applying option changes while accounting for all the complexity and edge cases but also keeping shader pack loading extremely fast took a ton of work. An intuitive and functional GUI on top of that was also a ton of work. All in all, the following people directly contributed code to shader config:

- coderbot: Shader config backend (loading / applying config files)
- FoundationGames: Shader configuration GUI (viewing / editing config files) + profile handling
- IMS: Fixes and tweaks to both the backend and GUI as well as overseeing QA / testing
- Justsnoopy30: Guidance based on experience with Starline, small fixes

## Other fixes

- Added support for the `prepare` pass (maximum#8760)
- Use the correct encoding when loading `.properties` files (IMS)
- Added some more hardcoded uniforms for Complementary while custom uniforms are still in development (IMS)
- Added support for `shadowTerrain`, `shadowTranslucent`, `shadowEntities`, `shadowBlockEntities` in shaders.properties (IMS)
- Added support for the `renderStage` uniform, allows SEUS PTGI HRR 3 to load (IMS)
    - HRR 3 is still not supported and has severe bugs on Iris.
- Added support for the `blendFunc` uniform (IMS)
- 1.18.1: Fix log spam related to `u_RegionOffset` (Justsnoopy30)

## Translations

- Updated pt_br.json by @ppblitto in #1202
- Updated zh_tw.json by @HJ-zhtw in #1173
- Updated zh_cn translations by @klkq in #1197
- Updated it_it.json (13 January 2022) by @devpelux in #1191
- Updated German translation by @Levelowel in #1179 and #1215
- Updated zh_tw.json by @HJ-zhtw in #1208
