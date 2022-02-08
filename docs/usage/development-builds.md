# Usage: Iris Development Builds

## Why are precompiled development builds not available online?

(aka "Why are there no GitHub Actions (CI) builds?")

As with all software, the versions of Iris actively being developed often have a number of changes from the currently released versions of Iris. This is a natural consequence of the fact that we only release a stable build once we've performed plenty of testing and review, and we only make a stable release once we have a bunch of features or bug fixes batched up as well.

As a result, it's often possible to get early access to new features by using development builds instead of the stable release builds. This has caused some users to compile development builds themselves and play with them, in order to access the new features. We are generally fine with this, because generally people who can figure out the steps to compile a build often can also figure out how to properly troubleshoot problems in development builds & format decent issue reports, allowing us to more easily fix issues before release.

However, this can become a problem when things like GitHub Actions artifact uploads make these development builds available to people who lack experience troubleshooting issues, because then we get unclear reports of issues that either duplicate information we already know, or fail to lead us closer to an underlying cause. It also causes problems for the support team if users go to them for support with in-development versions. **As a result, we've intentionally disabled mechanisms such as GitHub Actions artifact uploading.**


## Should I use development builds?

If you are experienced with finding the root cause of software problems, the general process of troubleshooting, have patience for problems and instability, know how to format sufficiently detailed issue reports, and want to help with development, then development builds might be a good choice for you!

If you just want a stable experience without facing unexpected crashes or new issues, then you should use the release builds.

Sometimes, we make beta builds available on the Discord server. These might not be as stable as release builds in some cases, but in other cases they fix a number of bugs and crashes present in the release builds. They're a bit between the stable builds and the development builds in that regard.


## How can I get development builds?

Once you're confident that development builds are right for you, follow these steps to get a build:

1. Download or check out a copy of the source code from GitHub, with the appropriate branch:
    - `trunk`, for Minecraft 1.16.5
    - `1.17`, for Minecraft 1.17.1
    - `1.18`, for Minecraft 1.18.1
    - a different development branch (usually for Minecraft 1.16.5)
2. Run `java -jar .\brachyura-bootstrap-0.jar build` in a terminal.
3. Done! Use the JAR file in `build/libs/` as a normal Fabric Mod.
