![Iris: An open-source shaders mod compatible with OptiFine shaderpacks](docs/banner.png)

# Iris

## Links

* **Visit [our website](https://irisshaders.github.io) for downloads and pretty screenshots!**
* Visit [our Discord server](https://discord.gg/jQJnav2jPu) to chat about the mod and get support! It's also a great place to get development updates right as they're happening.
* Visit [my Patreon page](https://www.patreon.com/coderbot) to support the continued development of Iris!


## Why did you make Iris?

Iris was created to fill a void that I saw in the Minecraft customization and graphical enhancement community: the lack of an open-source shaders mod that would let me load my favorite shader packs on modern versions of the game, while retaining performance and compatibility with modpacks. OptiFine, the current dominant mod for loading shader packs, has restrictive licensing that firmly stands in the way of any sort of tinkering, and is fairly notorious for having compatibility issues with the mods that I like. It's also mutually incompatible with Sodium, the best rendering optimization mod in existence by a large margin. ShadersMod was never updated past 1.12, and it lacks support for many of the many modern popular shaderpacks. So I created Iris, to try and solve these issues, and also address many other longstanding issues with shader packs.

I first and foremost develop Iris to meet my own needs of a performance-oriented shaders mod with good compatibility and potential for tinkering. Iris when paired with Sodium delivers great performance on my machine, finally making it fully possible for me to actually play with shaders instead of just periodically switching them on to take pretty screenshots, then switching them off once I get tired of frame drops. Of course, as it turns out, I'm far from the only person who benefits from the development of Iris, which is why I've decided to release it to the public as an open-source mod.

Canvas is another shaders mod that has already gained some traction. Its big downside for me, however, is the fact that it doesn't support the existing popular OptiFine shaderpacks that I want to use. This is because it uses a new format for shader packs that isn't compatible with the existing format, in order to achieve many of its goals for better mod integration with shaders. And while Canvas now has a few nice shaders like Lumi Lights, I still want to have the option of using existing shader packs that were designed for OptiFine. Shader packs just aren't interchangeable, just like how you cannot hand a copy of *The Last Supper* to someone who wants a copy of the *Mona Lisa*. They're both great pieces of art, but you absolutely cannot just swap one out for the other. That being said, if you're a fan of the shader packs available for Canvas, then great! Canvas and Iris are both perfectly fine ways to enjoy shaders with Minecraft.


## Goals

These are the goals of Iris. Since Iris isn't yet complete, it hasn't fully achieved all of these goals, though we are on the right track.

* **Performance.** Iris should fully utilize your graphics card when paired with optimization mods like Sodium.
* **Correctness.** Iris should try to be as issueless as possible in its implementation.
* **Mod compatibility.** Iris should make a best effort to be compatible with modded environments.
* **Backwards compatibility.** All existing ShadersMod / OptiFine shader packs should just work on Iris, without any modifications required.
* **Features for shader pack developers.** Once Iris has full support for existing features of the shader pipeline and is reasonably bug free, I wish to expand the horizons of what's possible to do with Minecraft shader packs through the addition of new features to the shader pipeline. Unlimited color buffers, direct voxel data access, and fancy debug HUDs are some examples of features that I'd like to add in the future.
* **A well-organized codebase.** I'd like for working with Iris code to be a pleasant experience overall.
* **Agnostic of Minecraft versions.** This is more of a long-term goal, but if it is possible to make the majority of Iris version-independent, then porting to new versions should be straightforward and quick. This theoretically works in the other direction as well, though Iris for 1.8.9 (and similar old versions) isn't very practical currently since Sodium doesn't exist for these versions of the game.


## But where's the Forge version?

Iris doesn't support Forge. This is for a few reasons:

* Sodium, which Iris depends on to achieve its great performance, does not support Forge. It's a long story, but in short: the lead developers of Forge were incredibly hostile to JellySquid when she developed for Forge, and since then have made no credible attempts to repair relations or even admit wrongdoing.
* My time is limited, and properly supporting all of the mods available for Forge (as well as Forge itself) is a huge amount of work. I don't play with Forge on modern versions of the game, and Forge is a continually shrinking segment of the Minecraft community.
* The Forge toolchain isn't really designed to play nice with mods like Iris that need to make many patches to the game code. It's possible, but Fabric & Quilt are just *better* for mods like Iris. It's no coincidence that the emergence of Fabric and the initial emergence of OptiFine replacements happened at around the same time.


## What's the current state of development?

Iris has a public stable release for 1.16 that works with a custom version of Sodium, and a public beta release for 1.17 that currently does not work with Sodium (currently, there is no official version of Sodium for 1.17). Iris is still in heavy development and gets new improvements every week, and is progressing very rapidly!

Compatibility with Sodium is an ongoing project. I've been chatting with JellySquid over the course of many months regarding compatibility, and many refactors and improvements have been implemented into Sodium in order to better accomondate Iris. Though a [fork of Sodium](https://github.com/IrisShaders/sodium-fabric) is still required, it's my intent that it will be unnecessary in the future. Official downloads of Iris include this modified version of Sodium for performance.


## What shader packs can I use right now?

Iris has been progressing quite rapidly recently. The following shader packs mostly work, though with a few bugs of course:

* [XorDev's shaderpacks](https://github.com/XorDev/Minecraft-Shaderpacks)
* [Sildur's Vibrant Shaders](https://sildurs-shaders.github.io/)
* [Sildur's Enhanced Default](https://sildurs-shaders.github.io/)
    * Enchantment glints are broken
* [Complementary Shaders](https://www.curseforge.com/minecraft/customization/complementary-shaders)
* [BSL Shaders](https://www.bitslablab.com/bslshaders/)
* [AstraLex Shaders](https://www.curseforge.com/minecraft/customization/astralex-shader-bsl-edit)
    * Water looks weird in general, though reflections do work. Weather doesn't work, rain does not show up and the sky does not darken.
* [SEUS v11](https://sonicether.com/shaders/download/v11-0/) (from 2016, not to be confused with SEUS PTGI E11)
    * General issues with weather
    * Note that SEUS v11 does not work on some platforms and might fail to compile.
* [SEUS Renewed](https://sonicether.com/shaders/download/renewed-v1-0-1/)
    * Note that SEUS Renewed does not work on some platforms and might fail to compile.
* [Skylec Shader](https://www.curseforge.com/minecraft/customization/skylec-shader) - a very lightweight shaderpack that pulls off some neat effects
    * Underwater is broken
    * Weather is broken

Other shaderpacks aren't officially supported currently.


## How can I help?

* The Iris Discord server is looking for people willing to provide support and moderate the server! Send @IMS#7902 a message if you'd like to apply.
* Code review on open PRs is appreciated! This helps get important issues with PRs resolved before I give them a look.
* Code contributions through PRs are also welcome! If you're working on a large / significant feature it's usually a good idea to talk about your plans beforehand, to make sure that work isn't wasted.

## Credits

* **TheOnlyThing and Vaerian**, for creating the excellent logo
* **Mumfrey**, for creating the Mixin bytecode patching system used by Iris and Sodium internally
* **The Fabric and Quilt projects**, for enabling the existence of mods like Iris that make many patches to the game
* **JellySquid**, for creating Sodium, the best rendering optimization mod for Minecraft that currently exists, and for making it open-source
* **All past, present, and future contributors to Iris**, for helping the project move along
* **Dr. Rubisco**, for maintaining the website
* **The Iris support and moderation team**, for handling user support requests and allowing me to focus on developing Iris
* **daxnitro, karyonix, and sp614x**, for creating and maintaining the current shaders mods

## License

Iris is completely free and open source, and you are free to read, distribute, and modify the code as long as you abide by the (fairly reasonable) terms of the [GNU LGPLv3 license](https://github.com/IrisShaders/Iris/blob/master/LICENSE).

Though it's not legally required, I'd appreciate it if you could ask before hosting your own public downloads for compiled versions of Iris. Though if you want to add the mod to a site like MCBBS, that's fine, no need to ask me.
