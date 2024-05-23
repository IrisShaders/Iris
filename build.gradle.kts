import org.ajoberstar.grgit.Grgit
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

object Constants {
    // https://fabricmc.net/develop/
    const val MINECRAFT_VERSION: String = "24w21b"
    const val FABRIC_LOADER_VERSION: String = "0.15.11"
    const val FABRIC_API_VERSION: String = "0.99.1+1.21"

    // https://semver.org/
    const val MOD_VERSION: String = "1.7.0"

    const val CUSTOM_SODIUM: Boolean = true
    const val CUSTOM_SODIUM_NAME: String = "sodium-fabric-0.5.8-snapshot+mc24w21b-local.jar"

    const val IS_SHARED_BETA: Boolean = false
    const val ACTIVATE_RENDERDOC: Boolean = false
    const val BETA_TAG: String = "DH Support"
    const val BETA_VERSION = 4

    const val SODIUM_VERSION: String = "mc1.20.5-0.5.8"
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
    id("fabric-loom") version "1.6.5"
    id("org.ajoberstar.grgit") version "5.2.2"
    id("com.github.gmazzo.buildconfig") version "5.3.5"
}

base {
    archivesName = "iris"

    group = "net.irisshaders"
    version = createVersionString()
}

loom {
    mixin {
        useLegacyMixinAp = false
    }

    accessWidenerPath = file("src/main/resources/iris.accesswidener")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

sourceSets {
    val main = getByName("main")
    val test = getByName("test")
    val headers = create("headers")
    create("desktop")
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

buildConfig {
    className("BuildConfig")   // forces the class name. Defaults to 'BuildConfig'
    packageName("net.irisshaders.iris")  // forces the package. Defaults to '${project.group}'
    useJavaOutput()

    buildConfigField("IS_SHARED_BETA", Constants.IS_SHARED_BETA)
    buildConfigField("ACTIVATE_RENDERDOC", Constants.ACTIVATE_RENDERDOC)
    buildConfigField("BETA_TAG", Constants.BETA_TAG)
    buildConfigField("BETA_VERSION", Constants.BETA_VERSION)

    sourceSets.getByName("desktop") {
        buildConfigField("IS_SHARED_BETA", Constants.IS_SHARED_BETA)
    }
}


java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = Constants.MINECRAFT_VERSION)
    mappings(loom.officialMojangMappings())
    modImplementation(group = "net.fabricmc", name = "fabric-loader", version = Constants.FABRIC_LOADER_VERSION)

    include("org.antlr:antlr4-runtime:4.13.1")
    modImplementation("org.antlr:antlr4-runtime:4.13.1")
    include("io.github.douira:glsl-transformer:2.0.1")
    modImplementation("io.github.douira:glsl-transformer:2.0.1")
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

    modCompileOnly(files(projectDir.resolve("DHApi.jar")))

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
    runClient {
        if (Constants.ACTIVATE_RENDERDOC && DefaultNativePlatform.getCurrentOperatingSystem().isLinux) {
            environment("LD_PRELOAD", "/usr/lib/librenderdoc.so")
        }
        jvmArgs("-Dmixin.debug.export=true")
    }
    getByName<JavaCompile>("compileDesktopJava") {
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }

    jar {
        from("${rootProject.projectDir}/LICENSE")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        val sodiumCompatibility = sourceSets.getByName("sodiumCompatibility")
        from(sodiumCompatibility.output)

        val vendored = sourceSets.getByName("vendored")
        from(vendored.output)

        val desktop = sourceSets.getByName("desktop")
        from(desktop.output)

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

    val isReleaseBuild = System.getProperty("build.release") != null
    val buildId = System.getenv("GITHUB_RUN_NUMBER")

    if (isReleaseBuild) {
        builder.append(Constants.MOD_VERSION)
    } else {
        builder.append(Constants.MOD_VERSION.substringBefore('-'))
        builder.append("-snapshot")
    }

    builder.append("+mc").append(Constants.MINECRAFT_VERSION)

    if (!isReleaseBuild) {
        val open = Grgit.open {
            dir = rootDir
        }

        if (buildId != null) {
            builder.append("-build.${buildId}")
        } else if (open == null) {
            builder.append("-local")
        } else {
            builder.append("-" + open.head().abbreviatedId + (if (!open.status().isClean) "-dirty" else ""))
        }
    }

    return builder.toString()
}
