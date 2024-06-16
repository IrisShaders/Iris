import net.fabricmc.loom.task.AbstractRemapJarTask

plugins {
    id("java")
    id("idea")
    id("fabric-loom") version "1.6.6"
    id("com.github.gmazzo.buildconfig") version "5.3.5"
}

val MINECRAFT_VERSION: String by rootProject.extra
val FABRIC_LOADER_VERSION: String by rootProject.extra
val FABRIC_API_VERSION: String by rootProject.extra
val SODIUM_VERSION: String by rootProject.extra

sourceSets.create("desktop")

buildConfig {
    className("BuildConfig")   // forces the class name. Defaults to 'BuildConfig'
    packageName("net.irisshaders.iris")  // forces the package. Defaults to '${project.group}'
    useJavaOutput()

    // TODO hook this up
    buildConfigField("IS_SHARED_BETA", false)
    buildConfigField("ACTIVATE_RENDERDOC", false)
    buildConfigField("BETA_TAG", "")
    buildConfigField("BETA_VERSION", 0)

    sourceSets.getByName("desktop") {
        buildConfigField("IS_SHARED_BETA", false)
    }
}

// This trick hides common tasks in the IDEA list.
tasks.forEach {
    it.group = null
}

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = MINECRAFT_VERSION)
    mappings(loom.officialMojangMappings())
    compileOnly("io.github.llamalad7:mixinextras-common:0.3.5")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.3.5")
    compileOnly("net.fabricmc:sponge-mixin:0.13.2+mixin.0.8.5")
    modCompileOnly("org.antlr:antlr4-runtime:4.13.1")
    modCompileOnly("io.github.douira:glsl-transformer:2.0.1")
    modCompileOnly("org.anarres:jcpp:1.4.14")

    modCompileOnly(files(rootDir.resolve("custom_sodium").resolve("sodium-fabric-1.20.1-0.6.0-snapshot+mc1.20.1-local.jar")))

    modCompileOnly(files(rootDir.resolve("DHApi.jar")))
}

tasks.withType<AbstractRemapJarTask>().forEach {
    it.targetNamespace = "named"
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

loom {
    mixin {
        defaultRefmapName = "iris.refmap.json"
        useLegacyMixinAp = false
    }

    accessWidenerPath = file("src/main/resources/iris.accesswidener")

    mods {
        val main by creating { // to match the default mod generated for Forge
            sourceSet("vendored")
            sourceSet("sodiumCompatibility")
            sourceSet("main")
        }
    }
}

tasks {
    getByName<JavaCompile>("compileDesktopJava") {
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }

    jar {
        from(rootDir.resolve("LICENSE.md"))

        val vendored = sourceSets.getByName("vendored")
        from(vendored.output.classesDirs)
        from(vendored.output.resourcesDir)

        manifest.attributes["Main-Class"] = "net.caffeinemc.mods.sodium.desktop.LaunchWarn"
    }
}
