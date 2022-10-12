# Usage: Iris Development Builds

## Should I use development builds?

If you are experienced with finding the root cause of software problems, the general process of troubleshooting, have patience for problems and instability, know how to format sufficiently detailed issue reports, and want to help with development, then development builds might be a good choice for you!

If you just want a stable experience without facing unexpected crashes or new issues, then you should use the release builds.

Sometimes, we make beta builds available on the Discord server. These might not be as stable as release builds in some cases, but in other cases they fix a number of bugs and crashes present in the release builds. They're a bit between the stable builds and the development builds in that regard.


## How can I get development builds?

Once you're confident that development builds are right for you, follow these steps to get a build:

1. Download or check out a copy of the source code from GitHub, with the appropriate branch:
    - `trunk`, for Minecraft 1.16.5
    - `1.18.2`, for Minecraft 1.18.2
    - `1.19`, for Minecraft 1.19
    - a different development branch (usually for Minecraft 1.16.5)
2. Execute a `java -jar brachyura-bootstrap-0.jar build` command in a terminal
3. Done! Use the JAR file in `build/libs/` as a normal Fabric Mod.
