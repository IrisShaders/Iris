# FAQ

## Table of Contents

- Why doesn't the Iris Installer work?
- How can I configure my shaders?
- Is my system supported?
- Why is (insert shader) not working?
- How do I disable my shaders?
- I added Iris, why is my game crashing?
- What shaders have been tested and are working with Iris?
- Will (insert feature) part of OptiFine be added to iris?

## Why doesn't the Iris Installer work?

You need java, please get it from: [Adoptium.net](https://adoptium.net/?variant=openjdk17&jvmVariant=hotspot)

## How can I configure my shaders?

Go to Options>Video Settings>Shader Packs>Shader Pack Settings.

## Is my system supported?

See the [Driver Support](usage/drivers.md) document for more information.

## Why is (insert shader) not working?

That shader likely isn't supported currently, but should be in the future. See the list of supported shaders [here](./supportedshaders.md)

## How do I disable my shaders?

Press K to disable shaders without using the GUI.

## I added Iris, why is my game crashing?

There are a few possible reasons:

- You're using an outdated version of Indium, Sodium Extras or Better Sodium Menu.
- You're using an unsupported shader pack.
- You're using macOS, which has limited support.
- You may have an incompatible mod installed.

## Why are entities becoming invisible randomly?

You probably have an outdated version of GraalVM installed. Install a normal version of Java or update to GraalVM 22.3 to fix the [issue](https://github.com/oracle/graal/issues/4849).

## What shaders have been tested and are working with Iris?

See [this](./supportedshaders.md) document for the list

## Will (insert feature) part of OptiFine be added to iris?

Iris is specifically a shaders mod. Other features are not planned for Iris. However, many other wonderful mods have been made by the fabric modding community which cover many of the features of OptiFine.

The following is a short list, more alternatives can be found through further research, the list is alphabetical.

Better Grass/Snow - [LambdaBetterGrass](https://modrinth.com/mod/lambdabettergrass)

Chunk Caching - [Bobby](https://modrinth.com/mod/bobby) -

Connected Textures - [Continuity](https://modrinth.com/mod/continuity)

Custom Item Textures - [CIT-Resewn](https://modrinth.com/mod/cit-resewn) -
You need to enable “Broken Paths” or MANY PACKS WILL NOT WORK.

Dynamic Lights - [LambDynamicLights](https://modrinth.com/mod/lambdynamiclights)

Smart Leaves - [Cull Leaves](https://modrinth.com/mod/cull-leaves)

Performance - Iris already requires [Sodium](https://modrinth.com/mod/sodium), but we also recommend [Lithium](https://modrinth.com/mod/lithium), [Hydrogen](https://modrinth.com/mod/hydrogen), and [FerriteCore](https://modrinth.com/mod/ferrite-core). You can also use either [Phosphor](https://modrinth.com/mod/phosphor) or [Starlight](https://modrinth.com/mod/starlight).

Various OptiFine features including toggles for animations, particles, and fog - [Sodium Extra](https://modrinth.com/mod/sodium-extra)

Zoom - [OkZoomer](https://modrinth.com/mod/ok-zoomer) or [Logical Zoom](https://modrinth.com/mod/logical-zoom), [Spy Zoom](https://modrinth.com/mod/spyzoom) for a vanilla alternative

You will also need [Indium](https://modrinth.com/mod/indium/) and [Fabric-API](https://modrinth.com/mod/fabric-api) for most mods as well as [ModMenu](https://modrinth.com/mod/modmenu) for settings.
