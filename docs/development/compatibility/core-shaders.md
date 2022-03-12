# On the incompatibility with core shaders & custom shaders added by mods

Or, "why don't my custom shaders work when a shader pack is active?"

Executive Summary: Custom shaders added by mods or resource packs are ignored by Iris when an Iris shader pack is loaded. Mods and resource packs using custom shader behavior / vertex formats are extremely difficult to accomodate within Iris.

This issue thread also has useful information: https://github.com/IrisShaders/Iris/issues/1042

This document is *not* intended to inform the reader on how to make their custom shaders compatible with Iris shader packs. Rather, it's intended to provide an explanation for why the incompatibility exists. It's not because we don't care about compatibility - it's because this is a hard problem and there is no general solution to it.


## Primer: What's a shader?

The word "shader" has many meanings in Minecraft. You might think that it refers to something that adds fancy graphics effects like bloom or reflections, or allows servers to add things like custom armor.

In actuality, a shader is just a program that runs on a graphics processor, whether that is a dedicated graphics card or an integrated graphics chip in a laptop. As with any program, a shader takes some data as input, and produces data as output. Conventionally, most shaders take vertex data, matrices, and textures as input, and produce pixels as output. Obviously, this is a simplification, but it gets the point across for this document.

## Shaders in Iris Shader Packs

Though Iris provides many features for shader packs, including support for shadow mapping, setting up certain inputs such as textures, uniforms, and extra vertex data, and ways to configure how the outputs of each shader program feed into other programs, most importantly it offers ways for shader packs to specify their own shader programs to be used for rendering content in Minecraft.

Critically, these custom shader programs allow shader packs to implement many custom effects for various types of in-world content, including applying dynamic shadows, changing lighting colors, and writing data to G-Buffers for deferred rendering. As these shader programs produce certain expected output colors, if a shader pack provides a certain shader program (such as a `gbuffers_terrain` program), it expects that program to run on all terrain and output colors to a certain framebuffer (or set of framebuffers).


## Tying things back to Minecraft

What happens when both a resource pack provides a custom shader program for a given piece of content & a shader pack provides a custom core shader program for that same content? There's a problem: both shader programs are provided as complete shader programs that take vertices and other inputs, and produce color outputs.

Given two arbitrary shader programs, there's no general sensible algorithm to "merge" them into a program that combines the behavior of both shader programs. Even writing out that previous sentence in a way that makes sense is difficult, because such a concept doesn't exist in computer science.


## Conclusion

By providing a high degree of control over Minecraft's rendering, Iris allows shader packs to accomplish advanced graphical effects. However, this comes at the natural expense of compatibility with other mods or resource packs that attempt to modify rendering through their own shaders.

Though it is possible to fix many compatibility issues between shader packs and mods through improving the implementation of shader mods like Iris, some sources of compatibility issues fundamentally cannot be fixed. Custom shader programs are one example.

As a result, it is strongly advised that mods that wish to maintain compatibility with Iris shader packs avoid using their own shader programs if at all possible. If mods do use shaders for some rendering, for the purposes of compatibility with mods like Canvas and Iris, it is recommended to create fallback rendering pathways that do not use custom shaders.
