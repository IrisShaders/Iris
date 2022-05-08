# Iris 1.2.3 Changelog
    
- Added optional debug commands
    - This gives more detailed debug output when OpenGL encounters errors, and will allow for more in the future.
- Replace ClientModInitializer with custom entrypoint, avoid dependency on FabricMC's constructor injection behavior, and move mod name to constant
    - This makes ports to other platforms easier for others.
- Added Unsafe version of XHFP writer
    - This improves performance in many scenarios.
- Added alpha tests overrides in the shadow pass for 1.16.5
- Added proper error detection for include resolution
- Fixed some issues with sky rendering in high-fog situations
- Avoid compiling Sodium shadow programs if the shader pack lacks a shadow pass
- Fixed block breaking lighting on Sildur's Vibrant
- Added fog start/end uniforms
- Attempt to fix compatibility with Better Foliage
- Fixed issues relating to changing lobbies on servers with shaders enabled
- Run shadow pass terrain setup and render before main terrain setup/render
- Use depth buffer provided by main Minecraft framebuffer instead of creating our own
- Port to Brachyura 0.80

## Translations

- Updated German translation (de_de.json) by @Isuewo and @douira in #1328
- Updated Polish translation (pl_pl.json) by @bascioTOja in #1334
- Updated Portuguese translation (pt_br.json) by @EmqsaGit in #1335
- Updated Russian translation (ru_ru.json) by @Ultra119 in #1350
- Added Ukrainian translation (ua_ua.json) by @Ultra119 in #1350
- Updated Chinese translation (zh_cn.json) by @Guanran928 in #1377
