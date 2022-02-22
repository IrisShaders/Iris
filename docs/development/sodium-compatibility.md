# Iris Development: Sodium Compatibility

Iris requires Sodium at runtime for performance reasons. However, obviously there are some challenges involved with compiling against an external mod.

Most of the code required for Sodium compatibility is in the sodiumCompatibility source set. We use Mixins to apply necessary modifications to Sodium, since Sodium doesn't yet provide formal APIs for many of the changes we need to make.

## Compiling and running without Sodium

⚠ **Running without Sodium isn't intended for normal use - most issues caused by running without Sodium have better workarounds.**

Sometimes it is useful to run Iris without Sodium for development or debugging purposes, such as when updating Iris to a version of Minecraft that doesn't have an available version of Sodium.

To do so, change the SODIUM line in `Buildscript.java` to the following:

```java
    static final boolean SODIUM = false;
```

Then, when running in development (such as with `./java -jar brachyura-bootstrap-0.jar runMinecraftClient`), you will be able to run Iris without Sodium. Note that doing this is only possible in a development environment.

Note that you shouldn't distribute JARs built with this method without adding a `breaks` clause for Sodium in the `fabric.mod.json`. Iris JARs without Sodium compatibility code will have severe issues when run with official versions of Sodium.
