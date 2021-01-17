# Iris

*A new shaders mod for Minecraft intended to be compatible with existing ShadersMod/Optifine shaders*

## What is Iris?

Iris is an experiment in loading shaderpacks on Fabric. After making minimal progress trying to hack karyonix's ShadersMod codebase on to newer versions, I finally asked: *How hard is it to load a basic shaderpack on Fabric from scratch?* As it turns out, nowhere near as hard as I expected! Thus, Iris was born.

Iris is completely free and open source, and you are free to read, distribute, and modify the code as long as you abide by the (fairly reasonable) terms of the [GNU LGPLv3 license](https://github.com/IrisShaders/Iris/blob/master/LICENSE). This should be a quite nice change of pace from the closed-source nature of OptiFine.

For the most part, I am creating Iris in order to have fun and get more experience in rendering. However, I am also frustrated by the constant incompatibilities and issues that players have reported when using OptiFine, and I also know that I am far from the only person with this experience. By developing a compatible and open source shaders mod, I hope to work towards a community where players and developers no longer have to worry about OptiFine incompatibilities.


## Current State

* Sildur's Vibrant Shaders and XorDev's shaderpacks work for the most part under Iris, and have been the focus of my development. However, most other shaderpacks either have severe rendering issues, or do not work at all. My current focus is to get Sildur's Vibrant Shaders and XorDev's shaderpacks to the point where they are 100% working before shifting focus to other shaderpacks. As I fix issues in these shaderpacks, other shaderpacks will very likely begin to work properly as well.
* I am working with JellySquid to make Sodium and Iris compatible. There is a proof-of-concept for Iris/Sodium compatibility available on a [custom fork of Sodium](https://github.com/IrisShaders/sodium-fabric) and [an experimental Iris branch](https://github.com/IrisShaders/Iris/tree/sodium-compatibility). While this proof of concept is being used as a reference for compatibility work, it will likely be replaced with more solid and stable code in the future.
* Iris is alpha quality software and is highly incomplete. If you are not a developer or familiar with compiling and running Minecraft mods from source, OptiFine is likely the better choice in the immediate moment. Once Iris is ready for more widespread use, precompiled builds will be published and distributed.


## Goals

First and foremost, Iris intends to be a *correct* and *compatible* shaders mod, and these two things come before everything else, including speed. Not to say that there won't be optimizations, because those are certainly planned: they're just a lower priority. However, especially in these early stages of the project, I'm entirely willing to sacrifice a few frames here and there in order to have a robust implementation reasonably free of bugs.

Iris also intends to have full feature parity with OptiFine shaderpacks. You should be able to install Iris, download a shaderpack, and have it just work out of the box.


## Non-Goals

At the moment, I have no intention of adding additional features to OptiFine shaderpacks. My primary goal is to reach feature parity with OptiFine, so that, at least in the area of shaders, the transition from OptiFine to Iris is seamless.

I also don't currently plan to port to Forge at the moment, because for one, OptiFine already works just fine with Forge. JellySquid has written up an [excellent document](https://gist.github.com/jellysquid3/629eb84a74ab326046faf971150dc6c3) on why she does not support Forge, and I agree for the most part as well. At the end of the day, this is a hobby project for fun, and supporting modern Forge just isn't fun for me. Hopefully you can understand.


## How can I help?

At the moment, the options for contributing are somewhat limited, and I don't have any formal systems in place. However, if you know a bit about rendering or the shader pipeline, you're free to fork the project and submit a pull request.


## Rationale

### What about Canvas?

[Canvas](https://github.com/grondag/canvas) is an advanced shader-based renderer for Minecraft also running on Fabric, by Grondag. This raises a common question: why not contribute to Canvas instead? I have already investigated this possibility, however the goals of Canvas and Iris are in fact quite different.

Canvas has the goal of creating a new shader pipeline focused on empowering mod authors with advanced graphics features, and is very promising. But this new shader pipeline is intentionally designed to not work with existing shaderpacks. Canvas wants to shed all of the backwards compatibility issues of loading existing shaderpacks, in order to achieve better performance and enable additional features for modders. For more information, see the "Why" section of the [Canvas README](https://github.com/grondag/canvas/blob/one/README.md#Why).

However, Iris has a notably different goal: loading existing shader packs out of the box. While it's theoretically possible to get this working on Canvas, I personally prefer to start off making a standalone shaders mod instead of spending time trying to understand another complex rendering system in addition to Minecraft and ShadersMod/OptiFine.

Canvas is progressing very rapidly, and has experimental support for things like sky shadows. [Lumi Lights](https://spiralhalo.github.io/) is a shaderpack for Canvas that offers features like bloom, godrays, reflective water, and more, and it's worth a shot if you're interested in a more stable experience than what Iris can currently offer.

I think that Canvas is a great project, and some day it could very well be the dominant shader mod for Fabric. Iris is made for those who still want to play with existing OptiFine shaderpacks, because there will always be legacy shaderpack content available, even when Canvas shaderpacks become more common.

In the long run, I think that it's better for everyone if we end up with Iris and Canvas competing rather than OptiFine and Canvas competing, because at the very least, there's opportunities for cooperation and collaboration between Iris and Canvas that just don't exist with OptiFine. It's also likely that Iris can implement a subset of Grondag's [FREX](https://github.com/grondag/frex) API, which could definitely result in FREX being more widely used.


### Why not OptiFabric / Optifine?

Another good point is: Since OptiFabric and Optifine exist already and (appear to) work just fine, what's the point of going through all of this effort anyways?

The fundamental flaw with OptiFabric is that it is a massive *hack*. I call it a hack in the traditional sense of the word: OptiFabric is very clever in implementation, yet it is also an inelegant and ultimately temporary solution. Most of the issues with OptiFabric are in fact problems with OptiFine itself, and no matter how good OptiFabric is, it can never work around these underlying architectural issues of OptiFine.

OptiFine is created as a "jar mod." Remember deleting META-INF about a decade ago (Wow, I feel old now) and copying the files from that one new mod directly into your `minecraft.jar`? Effectively, that's what Optifine now does every time you start the game. That means that it directly patches a decompiled version of Minecraft, then at runtime it overwrites the vanilla classes (units of code) with its own modified versions. This is about as invasive and incompatible as it sounds. Perhaps unsurprisingly, Optifine is *notorious* for having [many](https://github.com/TerraformersMC/Terrestria/issues/178), [many](https://github.com/jellysquid3/lithium-fabric/issues/73), [*many*](https://www.reddit.com/r/feedthebeast/comments/6ueyla/112_optifine_incompatible_with_some_mods/) incompatibilities with other mods.

On the other hand, Iris is a native Fabric mod that uses carefully crafted and precise code injections driven by the [Mixin](https://github.com/SpongePowered/Mixin) bytecode patching system. Mixin is the bases of the entire Fabric modding community, and is a much more compatible way of patching the game. This alone should help alleviate many of the compatibility issue that have plagued OptiFine.


## Credits

* **Vaerian**, for creating the excellent logo
* **daxnitro, karyonix, and sp614x**, for creating and maintaining the current shaders mods
* **Mumfrey**, for creating the finest bytecode patching system that I've ever had the pleasure of working with
* **The Fabric community**, for providing both support and great tooling
* **JellySquid**, for creating the first actually viable OptiFine alternative (as far as optimizations go)
* **All past, present, and future contributors to Iris**, for helping the project move along
