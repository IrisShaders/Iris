---
layout: default
---

# What is Iris?

Iris is a Minecraft mod built using the modern and elegant [Fabric Modloader](http://fabricmc.net) that aims to bring full support for [OptiFine](https://optifine.net) shaderpacks to Fabric. It will be compatible with [CaffeineMC](https://github.com/CaffeineMC) optimization mods such as Sodium in the near future, which will allow Iris to render breathtaking graphics while maintaining a low performance overhead.

Iris is completely free and open source, and you are free to read, distribute, and modify the code as long as you abide by the (fairly reasonable) terms of the [GNU LGPLv3 license](https://github.com/IrisShaders/Iris/blob/master/LICENSE). This should be a quite nice change of pace from the closed-source nature of OptiFine. For the most part, Iris started as a pet project, a way to have fun and get more experience in rendering. However, we at IrisShaders are also frustrated by the constant incompatibilities and issues that players have reported when using OptiFine, amd especially OpriFabric. By developing a compatible and open source shaders mod, we hope to work towards a community where players and developers no longer have to worry about OptiFine and it's incompatibilities.

## Where can I get Iris?

The team behind Iris plans to have full releases published on CurseForge for Minecraft Fabric 1.16.X and 1.17.X by June 8th, 2021. Until then, alpha versions are available from [GitHub](https://github.com/IrisShaders/Iris/releases) and the latest sources can be downloaded above. However, please note that Iris alpha releases and sources are unstable and highly incomplete. If you are not a developer or familiar with compiling and running Minecraft mods from source, OptiFine or OptiFabric is likely the better choice until after the full release.

That being said... **If you want to test out the newest features of the mod, please consider becoming a [Patron](https://www.patreon.com/coderbot), which will give you access to compiled alpha builds**. Alternatively, developers who know what they are doing can compile the mod themselves using the sources mentioned above.

## I'm having trouble with Iris. Where can I get help/support?

The [Iris Discord server](https://discord.gg/jQJnav2jPu) is home to a passionate community of mod developers and shader enthusiasts, many of whom would be more than willing to assist you with any mod trouble. **Please ask on Discord** before submitting an issue on GitHub, many problems are easily solved with a bit of troubleshooting, and you will likely get a much faster reply on Discord than on GitHub. In addition, the discord is a great place to chat about the mod and follow development.

## Iris Goals

First and foremost, Iris intends to be a *correct* and *compatible* shaders mod, and these two things come before everything else, including speed. Not to say that there won't be optimizations, because those are certainly planned: they're just a lower priority. However, especially in the early stages of the project, we are entirely willing to sacrifice a few frames here and there in order to have a robust implementation reasonably free of bugs.

Iris also intends to have full feature parity with OptiFine shaderpacks. You should be able to install Iris, download a shaderpack, and have it just work out of the box. Imagine that...

## Iris Non-Goals

At the moment, we have no intention of adding additional features to OptiFine shaderpacks. Our primary goal at IrisShaders is to reach feature parity with OptiFine, so that, at least in the area of shaders, the transition from OptiFine to Iris is seamless.

Iris will never support Forge. The devs only have a limited amount of time to dedicate towards Iris, and supporting Forge would take time away from other things that are more important. Between the fact that the lead developers of Forge are not pleasant people to work with and the fact that Forge was just never intended to support the kinds of things that Iris does, maintaining a Forge port just does not make sense. And if you want to use shaders with Minecraft Forge, there's always OptiFine...

## How can I help?

At the moment, the options for contributing are somewhat limited, and there aren't any formal systems in place. However, if you know a bit about rendering or the shader pipeline, you're free to fork the project and submit a pull request.

## Rationale

### What about Canvas?

[Canvas](https://github.com/grondag/canvas) is an advanced shader-based renderer for Minecraft also running on Fabric, by Grondag. This raises a common question: why not contribute to Canvas instead? We have already investigated this possibility, however the goals of Canvas and Iris are in fact quite different.

Canvas has the goal of creating a new shader pipeline focused on empowering mod authors with advanced graphics features, and is very promising. But this new shader pipeline is intentionally designed to not work with existing shaderpacks. Canvas wants to shed all of the backwards compatibility issues of loading existing shaderpacks, in order to achieve better performance and enable additional features for modders. For more information, see the "Why" section of the [Canvas README](https://github.com/grondag/canvas/blob/one/README.md#Why).

However, Iris has a notably different goal: loading existing shader packs out of the box. While it's theoretically possible to get this working on Canvas, We personally prefer to make a standalone shaders mod instead of spending time trying to understand another complex rendering system in addition to Minecraft and ShadersMod/OptiFine.

Canvas is progressing very rapidly, and has experimental support for things like sky shadows. [Lumi Lights](https://spiralhalo.github.io/) is a shaderpack for Canvas that offers features like bloom, godrays, reflective water, and more, and it's worth a shot if you're interested in a more stable experience than what Iris can currently offer.

Canvas is a great project, and some day it could very well be the dominant shader mod for Fabric. Iris is made for those who still want to play with existing OptiFine shaderpacks, because there will always be legacy shaderpack content available, even when Canvas shaderpacks become more common.

In the long run, it's probably better for everyone if we end up with Iris and Canvas competing rather than OptiFine and Canvas competing, because at the very least, there's opportunities for cooperation and collaboration between Iris and Canvas that just don't exist with OptiFine. It's also likely that Iris can implement a subset of Grondag's [FREX](https://github.com/grondag/frex) API, which could definitely result in FREX being more widely used.


### Why not OptiFabric / Optifine?

Another good point is: Since OptiFabric and Optifine exist already and (appear to) work just fine, what's the point of going through all of this effort anyways?

The fundamental flaw with OptiFabric is that it is a massive *hack*, in the traditional sense of the word: OptiFabric is very clever in implementation, yet it is also an inelegant and ultimately temporary solution. Most of the issues with OptiFabric are in fact problems with OptiFine itself, and no matter how good OptiFabric is, it can never work around these underlying architectural issues of OptiFine.

OptiFine is created as a "jar mod." Remember deleting META-INF about a decade ago and copying the files from that one new mod directly into your `minecraft.jar`? Effectively, that's what Optifine now does every time you start the game. That means that it directly patches a decompiled version of Minecraft, then at runtime it overwrites the vanilla classes (units of code) with its own modified versions. This is about as invasive and incompatible as it sounds. Perhaps unsurprisingly, Optifine is *notorious* for having [many](https://github.com/TerraformersMC/Terrestria/issues/178), [many](https://github.com/jellysquid3/lithium-fabric/issues/73), [*many*](https://www.reddit.com/r/feedthebeast/comments/6ueyla/112_optifine_incompatible_with_some_mods/) incompatibilities with other mods.

On the other hand, Iris is a native Fabric mod that uses carefully crafted and precise code injections driven by the [Mixin](https://github.com/SpongePowered/Mixin) bytecode patching system. Mixin is the basis of the entire Fabric modding community, and is a much more compatible way of patching the game. This alone should help alleviate many of the compatibility issues that have plagued OptiFine.

## Links

 * Support us on [Patreon](https://www.patreon.com/coderbot)
 * Join the [Iris Discord server](https://discord.gg/jQJnav2jPu)
 * View the source on [GitHub](https://github.com/IrisShaders/Iris)

## Credits

* **Vaerian**, for creating the excellent logo
* **daxnitro, karyonix, and sp614x**, for creating and maintaining the current shaders mods
* **Mumfrey**, for creating the finest bytecode patching system that I've ever had the pleasure of working with
* **The Fabric community**, for providing both support and great tooling
* **JellySquid**, for creating the first actually viable OptiFine alternative (as far as optimizations go)
* **All past, present, and future contributors to Iris**, for helping the project move along and become what it is today.

#### Thank You!



