# Debugging Shader Packs With Iris

Iris has some built-in functionality that can assist you in developing a shader pack for Iris. This page will give some context and explain how to use these features.

## Shader Code Transformation

Iris uses [glsl-transformer](https://github.com/IrisShaders/glsl-transformer) to apply various patches to the GLSL code of a shader pack before sending it to the driver for compilation. There are generally two types of patches being applied; those necessary for compatibility with Iris, and compatibility patches that are done to make sure the shader pack works on as many systems as possible. For reference, the compatibility patches can be found in [`CompatibilityTransformer`](/src/main/java/net/coderbot/iris/pipeline/transform/CompatibilityTransformer.java) while the other patches are grouped into the other files.

Some terms:

- **Translation Unit**: The GLSL code in a single file
- **External Declaration**: Anything at the top level of a translation unit, like a declaration or a function definition
- **Statement**: A single line of code in a function
- **Declaration** (Statement): The declaration of something in a function scope
- **Expression**: Code that evaluates to a value

### Effects of Shader Code Transformation

Roughly, glsl-transformer parses the GLSL code into an internal representation called an AST, applies the patches, and then prints the code back into a string. For better performance, the result of the patches is cached on multiple levels throughout the system. This results in faster loading times for shader packs that have already been loaded before or that have only been changed slightly.

Since the internal representation omits formatting, comments, whitespace and preprocessor directives (not including extensions and the version marker of course), none of these are preserved when printing the code back into a string. Because of this, the code that is printed back out is not necessarily the same as the code that was originally provided. This can make debugging shader packs a bit more difficult, since the line numbers in the error messages will not match up with the line numbers in the source code. A feature called line annotation is being worked on in glsl-transformer that aims to fix this issue by inserting `#line` directives into the code. In the interim, enabling the printing of the patched code provides a workaround for this issue (See [Pretty Printing](#pretty-printing)). Additionally, non-essential whitespace is omitted from the output for better performance, this can also be enabled by the user if required.

- The version statement is required. See [Version Statement](#version-statement) for more information.
- It is forbidden to use Iris internals, namely variables and functions prefixed with `iris_`, `irisMain` or `moj_import`. An exception will be thrown if these are encountered in the source code.

### Compatibility Patches

Since Iris aims to be compatible with many shader packs, hardware configurations and software environments, it is necessary to apply some compatibility patches to the shader packs to ensure wide compatibility. All known graphics drivers have some bugs or quirks that can cause issues with shader packs, and Iris aims to work around these issues where it is feasible.

The goal is to present a maximally uniform interface to the shader pack by smoothing out the differences between the drivers. Since many existing shader packs have been developed for Nvidia drivers and hardware, they sometimes rely on Nvidia-specific quirks. Furthermore, in some situations drivers may be more stringent by either adhering to the GLSL specification more closely or through their own limitations. Of course there are also bugs in the drivers that may need to be worked around. Some of these issues can be fixed by adding or removing specific pieces of code if necessary.

Naturally, shader packs that are currently in development could simply be fixed by their maintainers, but not all shader packs are still actively developed even if they have a significant following. Some shader packs don't test on certain systems which can also create incompatibilities that are often fixable through patching. **The compatibility patches are only applied if problematic pieces of code are detected, and they always print a warning message to the log prefixed with `(CompatibilityTransformer)` each time they do something**.

The following is a list of the compatibility patches that are applied to shader packs. Additional information on the specifics of each patch can be found in the [source code](/src/main/java/net/coderbot/iris/pipeline/transform/transformer/CompatibilityTransformer.java).

- Empty external declarations, which are just a single semicolon at the global level `;`, are removed. This is a workaround for a bug where they are not recognized as legal external declarations by some drivers. A semicolon following a function definition like `void main() {};` is in fact not part of the function definition but its own external declaration, namely an empty one.
- The `const` keyword is removed from declaration (statements) that refer to `const` parameters in their initializer expression, the part that comes after the `=`. Parameters with the `const` qualifier are not constant but rather immutable, meaning they may not be assigned to. Declarations with `const` are, depending on the driver and GLSL version, treated as constant meaning they only accept expressions that can be evaluated at compile time. Immutable parameters don't fulfill this requirement and thus cause a compilation error when they are used in the initializer of a constant. This is done to ensure better compatibility drivers' varying behavior at different versions. See Section 4.3.2 on Constant Qualifiers and Section 4.3.3 on Constant Expressions in the GLSL 4.1 and 4.2 specifications for more information. Additionally, see https://wiki.shaderlabs.org/wiki/Compiler_Behavior_Notes for the varying behavior of drivers.
- When an input variable, declared with `in`, is declared and used in a geometry or fragment shader, but there is no corresponding output variable in the shader of the previous stage, some drivers will error. To mitigate this, the missing declaration is inserted and initialized with a default value at the top of the `main` function.
- When the types of declared and used input and output variables don't match, some drivers will error. To mitigate this, the type of the declaration is changed to match the type in the later stage (the fragment shader). An internal variable with the original type is added so that the code can assign a value to it. At the end of the `main` function this value is either truncated or padded to patch the expected type.
- Unused functions are removed. Some drivers don't do certain semantic checks on unused functions which may result in errors when the code is then compiled with stricter drivers that do perform these checks before unused code removal. This heuristic is not perfect and may fail to remove unreachable functions that are used in another unreachable function.

### Version Statement

The version statement `#version <number> <profile>` where the number is required, but the profile is optionally one of `core` or `compatibility` is patched on versions starting with 1.17. For reference, the relevant code can be found in [`TransformPatcher`](/src/main/java/net/coderbot/iris/pipeline/transform/TransformPatcher.java) in the section afer `Root.indexBuildSession`. If the core profile is used or the profile is missing, and the version is at least 150, then only compatibility patches are applied, as long as the shader is not a vertex shader. On vertex shaders an exception is thrown in this case. They are required to either use a version lower than 330, which is then changed to be exactly 330, or declare the compatibility profile, which is then changed to the core profile.

The profile of compute shaders is always set to core.

## Debugging Features

### Pretty Printing

Printing the patched shader pack code as it is sent to the driver can be enabled by setting the following Java Argument:

```sh
-Diris.prettyPrintShaders=true
```

This will print the patched shader pack code to the `patched_shaders` folder that is created game instance folder. Any previous files in this folder will be deleted. The files are named based on the input files they originate from and are numbered based on their order of processing.

With this option enabled, the code is printed with indentation whitespace. Otherwise, you'll notice that the code has been effectively minified.

Also note that enabling this mode will slow down patching somewhat since more work is being done and files are written to disk.

### Debug Mode

A debug mode with additional features is being worked on.

## List of GLSL Language Specifications

- [GLSL 1.10](https://www.khronos.org/registry/OpenGL/specs/gl/GLSLangSpec.1.10.pdf)
- [GLSL 1.20](https://www.khronos.org/registry/OpenGL/specs/gl/GLSLangSpec.1.20.pdf)
- [GLSL 1.30](https://www.khronos.org/registry/OpenGL/specs/gl/GLSLangSpec.1.30.pdf)
- [GLSL 1.40](https://www.khronos.org/registry/OpenGL/specs/gl/GLSLangSpec.1.40.pdf)
- [GLSL 1.50](https://www.khronos.org/registry/OpenGL/specs/gl/GLSLangSpec.1.50.pdf)
- [GLSL 3.30](https://www.khronos.org/registry/OpenGL/specs/gl/GLSLangSpec.3.30.pdf)
- [GLSL 4.00](https://www.khronos.org/registry/OpenGL/specs/gl/GLSLangSpec.4.00.pdf)
- [GLSL 4.10](https://www.khronos.org/registry/OpenGL/specs/gl/GLSLangSpec.4.10.pdf)
- [GLSL 4.20](https://www.khronos.org/registry/OpenGL/specs/gl/GLSLangSpec.4.20.pdf)
- [GLSL 4.30](https://www.khronos.org/registry/OpenGL/specs/gl/GLSLangSpec.4.30.pdf)
- [GLSL 4.40](https://www.khronos.org/registry/OpenGL/specs/gl/GLSLangSpec.4.40.pdf)
- [GLSL 4.50](https://www.khronos.org/registry/OpenGL/specs/gl/GLSLangSpec.4.50.pdf)
- [GLSL 4.60](https://www.khronos.org/registry/OpenGL/specs/gl/GLSLangSpec.4.60.pdf)
