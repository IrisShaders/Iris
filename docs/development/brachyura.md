# Development: Using Brachyura

Iris, unlike most other Minecraft mods, uses the [Brachyura](https://github.com/CoolCrabs/brachyura) build system. Brachyura is a build tool created by CoolMineman / ThatTrollzer from the ground up to work for Minecraft modding. It is a Gradle + Fabric Loom replacement that aims to be efficient and allow the user to create fast and easy-to-debug build scripts.


## Initial IDE Import

- Close out of any IDEs you have open in the Iris project.
- Run `java -jar brachyura-bootstrap-0.jar idea`
    - If you are using an IDE other than IntelliJ IDEA, you will need to use the appropriate commands instead of `idea`:
      - VS Code:
        ```
        java -jar brachyura-bootstrap-0.jar jdt
        ```
        In VS Code it's probably helpful to also run the command `Java: Clean Java Language Server Workspace` afterwards.
      - Eclipse:
        ```
        java -jar brachyura-bootstrap-0.jar jdt
        ```
      - Netbeans:
        ```
        java -jar brachyura-bootstrap-0.jar netbeans
        ```
- Re-open your IDE and allow it to import (or re-import) the project.


## Running

You can either use the generated run configurations, or use `java -jar brachyura-bootstrap-0.jar runMinecraftClient`.


## Building a release JAR

Run `java -jar brachyura-bootstrap-0.jar build`. The resulting JAR file will be in `build/libs`.


## Editing the build script

The build script is a normal Java file at `buildscript/src/main/java/Buildscript.java`. If you want to use external dependencies, they must be added to `brachyurabootstrapconf.txt` in the appropriate format.

If you are using an IDE, make sure to use `java -jar brachyura-bootstrap-0.jar idea` (substituting `idea` as appropriate for your current IDE) to re-generate the project files for your edits to fully apply.


## Updating Brachyura

- Go to https://github.com/CoolCrabs/brachyura/releases
- Find the latest release
- Download the `brachyurabootstrapconf.txt` file and overwrite the existing `brachyurabootstrapconf.txt` at the root of the repository
- You might need to update `brachyura-bootstrap-0.jar` as well (this is rarely necessary)
- Manually restore the JGit dependency by adding the following lines at the end:

    ```
    https://repo.eclipse.org/content/groups/releases/org/eclipse/jgit/org.eclipse.jgit/6.0.0.202111291000-r/org.eclipse.jgit-6.0.0.202111291000-r.jar	a6184d0441ad4a912f73c2f9e14f2fe3826b9306	org.eclipse.jgit-6.0.0.202111291000-r.jar	true
    https://repo.eclipse.org/content/groups/releases/org/eclipse/jgit/org.eclipse.jgit/6.0.0.202111291000-r/org.eclipse.jgit-6.0.0.202111291000-r-sources.jar	81144d5f8866f92fdd93cae624e4f38d6ff92320	org.eclipse.jgit-6.0.0.202111291000-r-sources.jar	false
    https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.35/slf4j-api-1.7.35.jar	517f3a0687490b72d0e56d815e05608a541af802	slf4j-api-1.7.35.jar	true
    ```

- If you are using an IDE, make sure to use `java -jar brachyura-bootstrap-0.jar idea` (substituting `idea` as appropriate for your current IDE) to re-generate the project files for your edits to fully apply.
