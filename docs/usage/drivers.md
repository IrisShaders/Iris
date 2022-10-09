# Iris Driver Support

## Summary

Iris, Sodium, Minecraft, and system graphics drivers are all incredibly complex software. Some combinations work well, and some don't work at all. A list of known-functional (and known non-functional) configurations is provided below.

**If your configuration is marked as *❌ Not supported*, we ask that you please do not submit issue reports or make support requests to us.** We have determined that these platforms cannot possibly support Iris well, and chose not to spend time supporting them. Iris might function on these platforms, and might even function well in some limited cases, but we make no guarantees about its behavior, and trying to fix all the issues would be far too time-consuming for us.

- Windows:
  - Nvidia: ✅ Supported
  - AMD: ✅* Supported, some issues
  - Intel (2015+, Gen8 and above): ⚠ Partial support
  - Intel (HD 4000 series - HD 4000, HD 4400, etc): ⚠ Partial support
  - Intel (old, HD 3000 and below): ❌ Not supported
- macOS:
  - All drivers on Intel macs: ⚠ Deprecated, support will be removed in future releases
  - All drivers on ARM / M1 macs: ❌ Not supported, drivers have known crash issues with common shaders
- Linux:
  - Mesa (Intel, AMD): ✅* Supported, some issues
  - NVIDIA: ✅ Supported
  - Nouveau: ❌ Not supported
- Mobile devices (PojavLauncher, etc)
  - ❌ Not supported

## More details

- Windows:
  - NVIDIA: Fully supported. NVIDIA drivers generally provide the "best" user experience, but only loosely comply with the OpenGL standard and therefore are not an ideal choice for developing Iris or shader packs.
  - AMD: Mostly supported. AMD drivers are a little more strict but also have more bugs. Some shader packs (SEUS PTGI HRR 2.1, etc) do not work on AMD due to "NVIDIA-isms" from shader pack authors / Iris developers primarily testing with NVIDIA.
  - Intel: HD 3000 and below will most likely not work with Iris. More modern Intel integrated graphics chips might work, but often have issues, and often do not deliver great performance with most shader packs.
- macOS
  - Deprecated, will not be supported in the future, and we will spend no time investigating current issues. Things might work, things might not work. We're not able to do much since Apple has deprecated support for the industry-standard OpenGL API, which we depend on. M1 is particularly problematic.
  - In the future, support might be restored thanks to third parties developing compliant OpenGL driver implementations that work on macOS. This project in particular has quite some potential in that area: https://github.com/openglonmetal/MGL
- Linux
  - Mesa: Fully supported. Some shader packs have issues with Mesa, but most have workarounds. Please ensure you are on the latest version of Mesa before reporting issues.
  - NVIDIA (proprietary drivers): Same as Windows.
  - Nouveau: Not supported. These drivers have many bugs and extremely poor performance on almost all relevant NVIDIA cards.
  - Other drivers: Unknown.
- Mobile devices (PojavLauncher, etc)
  - Not supported. Android OpenGL ES drivers have huge amounts of bugs and poorly support most features of OpenGL ES. GL4ES has many bugs as well, and there are some features of OpenGL that cannot be clearly translated to OpenGL ES.

Some driver issues with specific GLSL features may be fixable through patching of the shader code. If you know of an incompatibility caused by a specific piece of code and potentially how to fix it using [glsl-transformer](https://github.com/IrisShaders/glsl-transformer), please contact us so that we can look into it together.

## What if I want my drivers / software / hardware to be supported?

Iris is an open-source project, and we welcome contributions from everyone. Choices of what platforms to support are based on what we're able to accomplish given our developer resources.

What this means is that if someone with relevant development experience wants their platform to be supported, and has the time to contribute bug fixes, contribute to the development of compliant drivers, or otherwise improve support, they're free to do so! For example, developers on macOS could investigate helping out with https://github.com/openglonmetal/MGL or similar projects.
