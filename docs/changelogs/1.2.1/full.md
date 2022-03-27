# Iris 1.2.1 Changelog

- Switched to the Brachyura build system, substantially speeding up the build process & allowing us to much more easily tweak and debug our build scripts (ThatTrollzer, coderbot, IMS)
  - Brachyura is still in development and is incomplete, but Iris is an early adopter. We hope that it will continue to stabilize and become appropriate for use by all Minecraft mods in due time.
  - Even in its incomplete state, Brachyura has already brought massive improvements over Gradle. Fixing build script issues has been a breeze, it's way faster, and hotswap & our run configs actually work properly now!
  - More information on using Brachyura in the context of Iris can be found [in our developer documentation](https://github.com/IrisShaders/Iris/blob/trunk/docs/development/brachyura.md).
- Added the entityShadowDistanceMul directive, allowing shader pack developers to change the render distance of entities and block entities in the shadow pass (IMS).
  - For example, `const float entityShadowDistanceMul = 0.5;` makes the render distance of entities & block entities in the shadow pass be half of the terrain render distance.
  - You have the option to expose this directly as a configuration option to the user just like with any other const float directive.
  - This is an Iris-exclusive feature for shader pack developers, requested by Emin.
- 1.16: Block entities are now culled properly in the shadow pass (IMS)
  - This also fixes a rare CME crash in some cases
- Reworked the entity hurt flash code, fixing a number of potential compatibility issues & rendering bugs (IMS, coderbot).
  - Items no longer flash red with the entity
  - Glowing entities now have a hurt flash applied
- Added support for the MC_HAND_DEPTH macro in shader packs (maximum#8760)
  - Fixes some shader packs failing to compile on Iris
- Fixed IrisApi::isShaderPackInUse incorrectly returning true when not in a world (coderbot).
- 1.16: Iris is now compatible with Physics Mod v2.6.6+ for 1.16 (IMS).
  - This removes the need for some extremely hacky raw ASM code that abused IMixinConfigPlugin
- 1.16: Optimized the terrain vertex format (IMS)
- Add shaderpack name, profile and changed options to crash reports (altrisi)
- Replaced a usage of IMixinConfigPlugin with normal Mixin features (coderbot)
  - `IMixinConfigPlugin`s are not an officially supported feature on Fabric, and Quilt plans to eventually drop support for them.
  - In total, we've removed 2 mixin plugins in this update, leaving 3 remaining. These 3 are primarily used to suppress logspam, and are mostly not critical, with one exception on 1.18 that will be fixed in the future.
  - We hope that https://github.com/FabricMC/fabric-loader/pull/524 or something similar will be able to replace our remaining use-cases for IMixinConfigPlugin.
- Marked Fade In Chunks as being explicitly incompatible. It cannot be compatible with shader packs (coderbot).
- Marked WorldEditCUI as incompatible on 1.17, and marked old versions of WorldEditCUI as incompatible on 1.18. WorldEditCUI 1.18.1+01 is compatible with this release of Iris (coderbot).
- 1.17+: Fixed some cases where Iris would break rendering of non-world things by incorrectly overriding shaders out of world rendering (IMS)
  - Fixes the "Ponder" feature of Create being invisible.
- 1.17+: Added support for the new_entity shader (IMS)
- 1.17+: Added support for geometry shaders outside the Sodium pipeline
  - Fixes entity shadows on SEUS PTGI.
- Added support for the extended vertex format on entities, block entities, and similar (IMS)
  - Fixes rendering issues with Advanced Materials on BSL, AstraLex, and similar packs
  - Fixes block breaking animation with KUDA shaders
  - Fixes many miscellaneous issues on other older packs

## Translations

- Updated Brazilian Portuguese translation (pt_br.json) by @ppblitto in #1220
- Updated sk_sk.json by @SmajloSlovakian in #1254
- Updated Turkish translation (tr_tr.json) by @egeesin in #1246
- Updated Czech translation (cs_cz.json) by @Fjuro in #1236
- Created Thai translation (th_th.json) by @NaiNonTH in #1221
- Updated Chinese translation (zh_cn.json) by @klkq in #1219
- Updated Taiwanese translation (zh_tw.json) by @HJ-zhtw in #1209
