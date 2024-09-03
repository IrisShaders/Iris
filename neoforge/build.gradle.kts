plugins {
    id("idea")
    id("maven-publish")
    id("net.neoforged.moddev") version "2.0.16-beta"
    id("java-library")
}


val MINECRAFT_VERSION: String by rootProject.extra
val NEOFORGE_VERSION: String by rootProject.extra
val MOD_VERSION: String by rootProject.extra
val SODIUM_FILE: String by rootProject.extra

base {
    archivesName = "iris-neoforge"
}

sourceSets {
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
    maven { url = uri("https://maven.neoforged.net/releases/") }

}

tasks.jar {
    from(rootDir.resolve("LICENSE"))
    manifest.attributes["Main-Class"] = "net.irisshaders.iris.LaunchWarn"
}

neoForge {
    // Specify the version of NeoForge to use.
    version = NEOFORGE_VERSION

    parchment {
        // Get versions from https://parchmentmc.org/docs/getting-started
        // Omit the "v"-prefix in mappingsVersion
        minecraftVersion = "1.21"
        mappingsVersion = "2024.07.28"
    }

    runs {
        create("client") {
            client()
            environment("LD_PRELOAD", "/usr/lib/librenderdoc.so")
        }
    }

    mods {
        create("sodium") {
            sourceSet(sourceSets.main.get())
        }
    }
}

val localRuntime = configurations.create("localRuntime")

dependencies {
    compileOnly(project(":common"))

    implementation("io.github.douira:glsl-transformer:2.0.1")
    additionalRuntimeClasspath("io.github.douira:glsl-transformer:2.0.1")
    jarJar("io.github.douira:glsl-transformer:[2.0.1,2.0.2]") {
        isTransitive = false
    }
    implementation("org.anarres:jcpp:1.4.14")
    additionalRuntimeClasspath("org.anarres:jcpp:1.4.14")
    jarJar("org.anarres:jcpp:[1.4.14,1.4.15]") {
        isTransitive = false
    }

    if (!rootDir.resolve("custom_sodium").resolve(SODIUM_FILE.replace("LOADER", "neoforge")).exists()) {
        throw IllegalStateException("Sodium jar doesn't exist!!! It needs to be at $SODIUM_FILE")
    }

    implementation(files(rootDir.resolve("custom_sodium").resolve(SODIUM_FILE.replace("LOADER", "neoforge"))))

    compileOnly(files(rootDir.resolve("DHApi.jar")))
}

// Sets up a dependency configuration called 'localRuntime'.
// This configuration should be used instead of 'runtimeOnly' to declare
// a dependency that will be present for runtime testing but that is
// "optional", meaning it will not be pulled by dependents of this mod.
configurations {
    runtimeClasspath.get().extendsFrom(localRuntime)
}

// NeoGradle compiles the game, but we don't want to add our common code to the game's code
val notNeoTask: (Task) -> Boolean = { it: Task -> !it.name.startsWith("neo") && !it.name.startsWith("compileService") }

tasks.withType<JavaCompile>().matching(notNeoTask).configureEach {
    source(project(":common").sourceSets.main.get().allSource)
    source(project(":common").sourceSets.getByName("vendored").allSource)
    source(project(":common").sourceSets.getByName("desktop").allSource)
    source(project(":common").sourceSets.getByName("sodiumCompatibility").allSource)
}

tasks.withType<Javadoc>().matching(notNeoTask).configureEach {
    source(project(":common").sourceSets.main.get().allJava)
}

tasks.withType<ProcessResources>().matching(notNeoTask).configureEach {
    from(project(":common").sourceSets.main.get().resources)
    from(project(":common").sourceSets.getByName("sodiumCompatibility").resources)
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

tasks.named("compileTestJava").configure {
    enabled = false
}
