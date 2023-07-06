# Iris 1.1.2 Changelog (Full)

Iris 1.1.2 has been released for Minecraft 1.16.5 and Minecraft 1.17.1! You can download the update at https://irisshaders.dev/download.html, or read the changelog below. A shorter and less detailed changelog [is also available](https://github.com/IrisShaders/Iris/blob/trunk/docs/changelogs/1.1.2/trimmed.md).

This update includes six weeks of bug fixes and other improvements, including some nice performance improvements! It's a fairly large update, and we recommend that everyone using previous versions of Iris update to this new version.

## Big Reworks

- Shadow culling has been completely rewritten. It no longer incorrectly culls chunks compared to OptiFine, and overall performance has improved greatly compared to the previous system (especially with shader packs like Enhanced Default, BSL, and Complementary Shaders).
    - An advanced system for shadow frustum culling with shader packs that do not use voxelization has been added that takes the player's view into account. If Iris can determine that something cannot cast a shadow anywhere in the player's view frustum, that object is not rendered.
    - This system detects when a shader pack like SEUS PTGI uses voxelization (through the presence of a geometry shader, Iris has no support for image load/store yet) and automatically disables this culling mode in that case to prevent bugs (such as light not being emitted from lights behind you).
    - This system also detects the sun-bounce GI in SEUS Renewed / SEUS v11 and automatically disables itself if the pack is configured to have sun-bounce GI enabled. If you disable the sun bounce GI (currently by editing the pack and in the future through the config GUI), shadow frustum culling will activate again.
    - Previous implementations of shadow culling had a bug with some packs, such as Sildur's Vibrant Shaders, that would cause shadows to be cast much less far away than they would on OptiFine. This issue has been completely fixed.
    - In the unlikely case that you observe a performance decrease, you can restore the old behavior using the newly-added Max Shadow Distance slider in Video Settings, which is active on packs that do not themselves specify a shadow render distance.
- Batched entity rendering has been completely rewritten, fixing a number of memory management, correctness, and performance issues.
    - A fixed pool of memory is used for entity buffers, avoiding issues with extreme allocation rates (and OutOfMemoryErrors) that the previous implementation suffered from. As a result, staring at a charged creeper or newly-created wither no longer crashes the game.
- The modified version of Sodium bundled in Iris for Minecraft 1.17.1 is now based on Sodium 0.3.2, fixing many driver compatibility bugs and performance issues.

## New Shader Features and Fixes

- Fog now fully works in most shader packs, including Sildur's Enhanced Default and other vanilla-like shaders
    - fogMode and fogDensity are fully supported on 1.16.5
    - fogMode is fully supported on 1.17, fogDensity is yet not supported on 1.17
- Entities now flash red when hurt, and flash white when they are going to explode
    - entityColor is now fully supported
    - The overlay texture is now stored in texture unit 2, not texture unit 6. Eventually we will want to restore vanilla behavior of storing it in texture unit 1.
- End portals now properly render with most shader packs
    - The "simplified" end portal rendering mode expected by shaders is now supported.
    - blockEntityId and gbuffers_block are now fully supported
- Complementary Shader's IntegratedPBR for entities now mostly works
    - entityId is fully supported, and gbuffers_entities is now properly used.
- Iris now has initial support for the path-traced lighting in SEUS PTGI, and has some fixes to the lighting in SEUS Renewed.
    - The water is still broken in SEUS PTGI due to the lack of support for the "custom textures" feature of the shader format.
    - Geometry shaders are now always supported on 1.16, and are supported for terrain on 1.17.
    - The shadow color buffers are now cleared to white instead of black.
    - oldLighting is now supported.
    - ambientOcclusionLevel is now supported (credit IMS212).
    - Shadow mipmap directives are now supported.
    - GL_NEAREST directives for shadow textures are now fully supported.

## Mod Compatibility

- Iris is now fully compatible with Physics Mod (thanks IMS212)
    - Though 1.1.1 was compatible, there were a few bugs and issues.
    - Credit goes to haubna (the author of Physics Mod) for giving us access to the source code of Physics Mod in order aid with debugging & fixing the compatibility issues.
- Added a new screen for when you have an incompatible version of Sodium installed.
    - Iris bundles a compatible modified version of Sodium with all official releases.
    - A common user mistake is installing an official build of Sodium, which Iris does not support.
    - This screen replaces an unclear, cryptic crash.
- Iris is once again compatible with DashLoader when using Complementary Shaders
    - atlasSize has a fallback path to support DashLoader
- Iris now has integration with Mod Menu, allowing you to open the Shader Pack selection screen from within Mod Menu. (credit Grayray75)
- Initial compatibility with Immersive Portals is now available on 1.17.1, though there are still issues (credit qouteall for implementing the compatibility code)
    - Immersive Portals and Iris are incompatible on 1.16.5.
- Fixed an issue where directional shading would be enabled on some blocks rendered with the Fabric Rendering API, even if the shader pack disabled it.
    - Iris no longer relies on BakedQuad to disable directional shading.
    - This fixes issues with Connected Block Textures, CTM-Fabric, and NCTM.


## Other Miscellaneous Fixes

- Iris is much more selective about what messages it prints to the log now. Previous versions printed a huge amount of messages to the log, many of them not being very useful, and a bunch of these messages were actually wrong. This is pretty much fixed.
- Translucent falling blocks no longer render through solid terrain with some packs
    - This was caused by gbuffers_terrain being used, instead of gbuffers_entities
- A number of small tweaks and bug fixes from the Starline fork (credit Justsnoopy30) have been merged, including:
    - TranslationStorage#hasTranslation now works properly with overriden translations from shader packs.
    - Added an explicit line in F3 for when shaders are disabled (instead of "[Iris] Shaderpack: (off)")
    - The correct character encoding is now used to read `.properties` files from shader packs
- A few unnecessarily-large / unused files have been removed from the released Iris JAR file
    - The file size of the mod icon has been substantially reduced without significant sacrificies to quality (credit RDKRACZ / KORR for pointing this out)
- Fixed some unclear / bad error messages in shader pack loading
    - Messages no longer contain "Optional[]"
    - A stack trace is no longer printed on NoSuchFileException errors
    - Shader pack loading errors no longer fatally crash the game on startup
- Fixed an issue where Complementary's fancy nether portal effects in the v4.2 dev versions would break in some cases
    - A dot product wasn't being computed properly in vertex writing code.
- You can now enable Fabulous Graphics when shaders are fully disabled.
- Fixed an issue where some blocks would be see-through with Continuum Shaders
    - An unexpected sign extension in vertex writing caused part of mc_midTexCoord to get corrupted.
    - This was highly dependent on resource packs and load order.

## 1.17-specific fixes

- End gateway beams no longer appear to render twice with shader packs that use shadows.
- Fixed separate leads incorrectly rendering as connected
- Block selection outlines now render properly, even with world curvature enabled.
- Chunk borders (F3 + G), hitboxes, and other lines now render properly with shaders enabled.
    - Chunk borders and hitboxes are properly colored and lit as well
- Fixed a shader patch failure when an "off" alpha test was specified in shaders.properties
- World borders render properly with most packs now.
- The energy swirl around charged creepers now renders properly.

## Translations

- Updated French (fr_fr) translation (credit toinouH, maxigator)
- Updated German (de_de) translations (credit phibr0)
- Updated Japanense (ja_jp) translations (credit Propomp)
- Added Korean (kr_kr) translation (credit CoderStone)
- Added Turkish (tr_tr) translation (credit EminGT)
- Added Polish (pl_pl) translation (credit bascioTOja)
- Added Swiss German (de_ch) translation (credit CodingRays)
- Added Estonian (et_ee) translation (credit Madis0)

## Looking to the Future

This update took much longer to come out than I'd like, and had several development roadblocks along the way. A big issue was that for many weeks, the devlopment branches had big performance regressions that made them unreleasable. While these regressions were fixed (and replaced with performance improvements), this completely stalled whatever update cycle that Iris might have had.

In particular, both of the rewrites of nontrivial parts of the code (which were essentially forced due to issues that came up along the way) took up a very large amount of time. In the future, I would like to move towards a system where we release smaller updates more frequently, with the goal of having a new release every week or every other week. As a part of this, I will have a goal of ensuring that the primary development branches are always in a releaseable (or close to being releasable) state.

While each of these smaller future updates might be less exciting, releasing more often and more consistently will mean that more features get released overall, and more quickly too.

## Internals

- Ignore `.DS_Store`, `.AppleDouble`, and `.LSOverride` files on macOS to prevent built jars from always being marked with `-dirty`
- Internal shaders were deleted from the source tree (they were already inaccessible, but still present in the code)
- Java 16 is used with GitHub Actions on all versions.
- Updated Gradle to version 7.2
- Iris Explorer is now explicitly marked as incompatible with modern Iris since Iris includes its own shader selection screen.
- Fixed some compiler warnings.
