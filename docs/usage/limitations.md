# Limitations of Iris

There's a number of issues in Iris that are fundamental limitations of Iris and the platforms it runs on. We choose not to maintain these issues on the issue tracker because they can feasibly not be fixed, or it is not worth our time to attempt to work around them or fix them. We feel that it's better to have these limtations properly documented instead of having issues on the trackers that we won't ever close.

1. [Iris doesn't fully work with all drivers, notably Apple's drivers on macOS.](drivers.md)
2. [Iris doesn't work with certain mods or features of mods, because these mods do things that Iris cannot support.](incompatible-mods.md)
3. [Some shader packs have bugs and shortcoings that are not the fault of Iris](../ShaderpackBugs.md). Some of these are limitations in the way that the shader pack is designed.
4. Iris cannot support resource packs using custom core shaders, including:
    - Custom armor shaders used on many servers, such as Origin Realms
    - The workaround resource pack for https://bugs.mojang.com/browse/MC-164001
