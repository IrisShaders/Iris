object Constants {
    // https://fabricmc.net/develop/
    const val MINECRAFT_VERSION: String = "24w10a"
    const val FABRIC_LOADER_VERSION: String = "0.15.6"
    const val FABRIC_API_VERSION: String = "0.96.8+1.20.5"

    // https://semver.org/
    const val MOD_VERSION: String = "1.7.0"

    const val CUSTOM_SODIUM: Boolean = true
    const val CUSTOM_SODIUM_NAME: String = "sodium-fabric-0.5.8-snapshot+mc24w09a-local.jar"

    const val SODIUM_VERSION: String = "mc1.20.4-0.5.8"
}

repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
}

plugins {
    // Unlike most projects, we choose to pin the specific version of Loom.
    // This prevents a lot of issues where the build script can fail randomly because the Fabric Maven server
    // is not reachable for some reason, and it makes builds much more reproducible. Observation also shows that it
    // really helps to improve startup times on slow connections.
    id("fabric-loom") version "1.5.7"
}

base {
    archivesName = "iris"

    group = "net.irisshaders"
    version = createVersionString()
}

loom {
    mixin {
        defaultRefmapName = "iris.refmap.json"
    }

    accessWidenerPath = file("src/main/resources/iris.accesswidener")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

sourceSets {
    val main = getByName("main")
    val test = getByName("test")
    val headers = create("headers")
    val vendored = create("vendored")
    val sodiumCompatibility = create("sodiumCompatibility")

    headers.apply {
        java {
            compileClasspath += main.compileClasspath
        }
    }

    test.apply {
        java {
            compileClasspath += main.compileClasspath
            compileClasspath += main.output
        }
    }

    vendored.apply {
        java {
            compileClasspath += main.compileClasspath
        }
    }

    sodiumCompatibility.apply {
        java {
            compileClasspath += main.compileClasspath
            compileClasspath += main.output
        }
    }

    main.apply {
        java {
            compileClasspath += headers.output
            compileClasspath += vendored.output
            runtimeClasspath += vendored.output
            runtimeClasspath += sodiumCompatibility.output
        }
    }
}

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = Constants.MINECRAFT_VERSION)
    mappings(loom.officialMojangMappings())
    modImplementation(group = "net.fabricmc", name = "fabric-loader", version = Constants.FABRIC_LOADER_VERSION)

    include("org.antlr:antlr4-runtime:4.11.1")
    modImplementation("org.antlr:antlr4-runtime:4.11.1")
    include("io.github.douira:glsl-transformer:2.0.0-pre13")
    modImplementation("io.github.douira:glsl-transformer:2.0.0-pre13")
    include("org.anarres:jcpp:1.4.14")
    modImplementation("org.anarres:jcpp:1.4.14")

    if (Constants.CUSTOM_SODIUM) {
        modImplementation(files(projectDir.resolve("custom_sodium").resolve(Constants.CUSTOM_SODIUM_NAME)))
    } else {
        modImplementation("maven.modrinth", "sodium", Constants.SODIUM_VERSION)
    }

    // Sodium dependencies
    modRuntimeOnly(fabricApi.module("fabric-rendering-fluids-v1", Constants.FABRIC_API_VERSION))
    modRuntimeOnly(fabricApi.module("fabric-rendering-data-attachment-v1", Constants.FABRIC_API_VERSION))
    modRuntimeOnly(fabricApi.module("fabric-resource-loader-v0", Constants.FABRIC_API_VERSION))
    modRuntimeOnly(fabricApi.module("fabric-block-view-api-v2", Constants.FABRIC_API_VERSION))
    modRuntimeOnly(fabricApi.module("fabric-block-view-api-v2", Constants.FABRIC_API_VERSION))

    modCompileOnly(files(projectDir.resolve("custom_sodium").resolve("DistantHorizons-fabric-2.0.2-a-dev-1.20.4.jar")))

    fun addEmbeddedFabricModule(name: String) {
        val module = fabricApi.module(name, Constants.FABRIC_API_VERSION)
        modImplementation(module)
        include(module)
    }

    // Fabric API modules
    addEmbeddedFabricModule("fabric-api-base")
    addEmbeddedFabricModule("fabric-key-binding-api-v1")
}

tasks {

    jar {
        from("${rootProject.projectDir}/LICENSE")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        val sodiumCompatibility = sourceSets.getByName("sodiumCompatibility")
        from(sodiumCompatibility.output.classesDirs)
        from(sodiumCompatibility.output.resourcesDir)

        val vendored = sourceSets.getByName("vendored")
        from(vendored.output.classesDirs)
        from(vendored.output.resourcesDir)

        from (sodiumCompatibility.output) {
            this.filesMatching("*refmap.json") {
                this.name = "iris-sodium-compat-refmap.json"
            }
        }

        manifest.attributes["Main-Class"] = "net.irisshaders.iris.LaunchWarn"
    }

    processResources {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version))
        }
    }
}

// ensure that the encoding is set to UTF-8, no matter what the system default is
// this fixes some edge cases with special characters not displaying correctly
// see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

fun createVersionString(): String {
    val builder = StringBuilder()

    val isReleaseBuild = project.hasProperty("build.release")
    val buildId = System.getenv("GITHUB_RUN_NUMBER")

    if (isReleaseBuild) {
        builder.append(Constants.MOD_VERSION)
    } else {
        builder.append(Constants.MOD_VERSION.substringBefore('-'))
        builder.append("-snapshot")
    }

    builder.append("+mc").append(Constants.MINECRAFT_VERSION)

    if (!isReleaseBuild) {
        if (buildId != null) {
            builder.append("-build.${buildId}")
        } else {
            builder.append("-local")
        }
    }

    return builder.toString()
}
