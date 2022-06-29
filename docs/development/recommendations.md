# Recommendations for development

Once you got the basics of the project setup, you might be wondering how to improve the development process on your computer.
Here are a few recommendations to help you get started.

- Use runMinecraftClient or the Minecraft Client run configuration when making changes
    - These let you immediately see the changes you make in the development environment, without making a new JAR file and adding it to a production environment.
- Add LazyDFU to your mods folder
    - LazyDFU makes the game start up faster, by deferring DFU compiling until it's needed.
      - Download LazyDFU from CurseForge and place it in the run/mods folder to add it.
- Use IntelliJ + the Minecraft Development plugin
    - This plugin does a lot for you, including but not limited to:
        - Auto completing mixin annotations
        - Creating access wideners easier
        - Creating a new mod with a template
        - Mixin error-checking
        - Workspace highlighting
- If you are facing an issue that can only be easily reproduced on servers, use the [AuthMe](https://www.curseforge.com/minecraft/mc-mods/auth-me) mod to log in while using the development environment. This is often way easier than using print statement debugging & constantly recompiling the mod.
