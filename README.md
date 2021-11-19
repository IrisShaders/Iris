![Iris: An open-source shaders mod compatible with OptiFine shaderpacks](docs/banner.png)

# Iris

## Links

* **Visit [our website](https://irisshaders.net) for downloads and pretty screenshots!**
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


## What's the current state of development?

Iris has public releases for 1.16 and 1.17 that work with custom versions of Sodium. Iris has made a lot of progress, but it is still not complete software. Though it is already usable for many people, and generally provides a big performance boost over alternatives, there is still much to be done before Iris can be a complete replacement for OptiFine's shaders features. As Iris continues its development, it will only become more and more complete with time.

Compatibility with Sodium is an ongoing project. I've been chatting with JellySquid over the course of many months regarding compatibility, and many refactors and improvements have been implemented into Sodium in order to better accommodate Iris. Though a [fork of Sodium](https://github.com/IrisShaders/sodium-fabric) is still required, it's my intent that it will be unnecessary in the future. Official downloads of Iris include this modified version of Sodium for performance.


## What shader packs can I use right now?

The following shader packs mostly work, though with a few bugs of course:

* [Sildur's Vibrant Shaders](https://sildurs-shaders.github.io/)
* [Complementary Shaders](https://www.curseforge.com/minecraft/customization/complementary-shaders)
* [BSL Shaders](https://www.bitslablab.com/bslshaders/)
* [Sildur's Enhanced Default](https://sildurs-shaders.github.io/)
    * Enchantment glints are broken
* [SEUS Renewed](https://sonicether.com/shaders/download/renewed-v1-0-1/)
    * Note that SEUS Renewed does not work on some platforms and might fail to compile.
* [SEUS v11](https://sonicether.com/shaders/download/v11-0/) (from 2016, not to be confused with SEUS PTGI E11)
    * General issues with weather
    * Note that SEUS v11 does not work on some platforms and might fail to compile.
* [Skylec Shader](https://www.curseforge.com/minecraft/customization/skylec-shader) - a very lightweight shaderpack that pulls off some neat effects
    * Underwater is broken
    * Weather is broken
* [XorDev's shaderpacks](https://github.com/XorDev/Minecraft-Shaderpacks)

Other shaderpacks aren't officially supported currently.


## How can I help?

* The Iris Discord server is looking for people willing to provide support and moderate the server! Send @IMS#7902 a message if you'd like to apply.
* Code review on open PRs is appreciated! This helps get important issues with PRs resolved before I give them a look.
* Code contributions through PRs are also welcome! If you're working on a large / significant feature it's usually a good idea to talk about your plans beforehand, to make sure that work isn't wasted.


## But where's the Forge version?

Iris doesn't support Forge. This is for a few reasons:

* My time is limited, and properly supporting all the mods available for Forge (as well as Forge itself) is a huge amount of work. When people ask for Forge support, they aren't asking just for Iris to run on Forge, they are also asking for it to be compatible out of the box with their kitchen sink modpack that contains over 300 mods. As a result, properly supporting Forge would require me to divert large amounts precious time into fixing tedious compatibility issues and bugs, time that could instead be spent making the Fabric version of Iris better.
* The Forge toolchain isn't really designed to play nice with mods like Iris that need to make many patches to the game code. It's possible, but Fabric & Quilt are just *better* for mods like Iris. It's no coincidence that the emergence of Fabric and the initial emergence of OptiFine replacements happened at around the same time.
* Sodium, which Iris depends on to achieve its great performance, has no official Forge version. It's a long story, but in short: the lead developers of Forge were incredibly hostile to JellySquid when she developed for Forge, and since then have made no credible attempts to repair relations or even admit wrongdoing.

The license of Iris does permit others to legally port Iris to Forge, and we are not strictly opposed to the existence of an Iris Forge port created by others. However, what we are opposed to is someone doing a bare-minimum port of Iris to Forge, releasing it to the public, and then abandoning it or poorly maintaining it while compatibility issues and bug reports accumulate. When that happens, not only does that hurt the reputation of Iris, but we also ultimately get flooded by users wanting support with a low-effort Forge port that we didn't even make.

So, if you want to distribute a Forge port of Iris, we'd prefer if you let us know. Please don't just name your port "Iris Forge," "Iris for Forge," or "Iris Forge Port" either. Be original, and don't just hijack our name, unless we've given you permission to use one of those kinds of names. If a well-qualified group of people willing to maintain a Forge port of Iris does appear, then a name like "Iris Forge" might be appropriate - otherwise, it probably isn't appropriate.


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

## FAQ
> Q. How do I download Iris?

A. Please visit the official Iris website: https://irisshaders.net/

> Q. Why doesn't the Iris Installer work?

A. You need java, please get it from: https://adoptium.net/?variant=openjdk17&jvmVariant=hotspot

> Q. How can I configure my shaders?

A. [#663](https://github.com/IrisShaders/Iris/issues/663)

> Q. Why is (insert shader) not working?

A. That shader likely currently isn't supported, but should be in the future.

> Q. How do I disable my shaders?

A. Press K to disable shaders without using the GUI.

> Q. I added Iris, why is my game crashing?

A. There are a few possible reasons:

• You're using an outdated version of Indium, Sodium Extras or Better Sodium Menu.

• You're using an unsupported shader pack.

• You're using macOS, which has limited support.

• You may have an incompatible mod installed. 

> Q. What shaders have been tested and are working with Iris?

A. The following shaders have been tested and work well:

• Complementary Shaders (https://www.curseforge.com/minecraft/customization/complementary-shaders)

• Prismarine Shaders (https://www.curseforge.com/minecraft/customization/prismarine-shader)

• BSL Shaders (https://bitslablab.com/bslshaders/)

• Continuum 1.3 

• Continuum 2.0.4

• SEUS V11 (https://www.sonicether.com/seus/)

• SEUS Renewed (https://www.sonicether.com/seus/)

• Sildur's Enhanced Default (https://sildurs-shaders.github.io/)

• Sildur's Vibrant Shaders (https://sildurs-shaders.github.io/)

• Skylec v4.0 (https://www.curseforge.com/minecraft/customization/skylec-shader)

• Tea V1.5.3

• XorDev's shaders

• RedHat shaders (https://www.curseforge.com/minecraft/customization/redhat-shader-v1-chocapic13-edit)

The following shaders have been tested and work with some minor issues:

• Project Luma [No Sky, broken end portals/gateways]

• Triliton V8 [reflections broken]

• Stereo's Default+ [No sky on 1.17] 

> Q. Will (insert feature) part of OptiFine be added to iris? 

A. Iris is specifically a shaders mod. Other features are not planned for Iris. However, many other wonderful mods have been made by the fabric modding community which cover many of the features of OptiFine. 

The following is a short list, more alternatives can be found through further research, the list is alphabetical.

Better Grass/Snow - https://www.curseforge.com/minecraft/mc-mods/lambdabettergrass

Chunk Caching - https://www.curseforge.com/minecraft/mc-mods/bobby
Bobby is incompatible with Starlight.

Connected Textures - https://modrinth.com/mod/continuity

Custom Item Textures - https://modrinth.com/mod/cit-resewn 
You need to enable “Broken Paths” or MANY PACKS WILL NOT WORK. 

Dynamic Lights - https://www.curseforge.com/minecraft/mc-mods/lambdynamiclights

Smart Leaves - https://www.curseforge.com/minecraft/mc-mods/cull-leaves 

Performance - Iris is already bundled with Sodium but we also recommend Lithium (https://modrinth.com/mod/lithium), Hydrogen (https://modrinth.com/mod/hydrogen), and FerriteCore (https://www.curseforge.com/minecraft/mc-mods/ferritecore-fabric). You can also use either Phosphor (https://modrinth.com/mod/phosphor) or Starlight (https://modrinth.com/mod/starlight). Phosphor does not have a 1.17 version yet.

Various OptiFine features including toggles for animations, particles, and fog - https://www.curseforge.com/minecraft/mc-mods/sodium-extra

Zoom - https://www.curseforge.com/minecraft/mc-mods/ok-zoomer or https://www.curseforge.com/minecraft/mc-mods/logical-zoom, https://modrinth.com/mod/spyzoom for a vanilla alternative

You will also need https://modrinth.com/mod/indium/ and https://www.curseforge.com/minecraft/mc-mods/fabric-api for most mods as well as https://www.curseforge.com/minecraft/mc-mods/modmenu for settings.

If you need further help please ask our helpers and mods in iris-support, and someone will be there shortly to assist you! 
