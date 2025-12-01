plugins {
    id("idea")
    id("net.neoforged.moddev") version "2.0.107"
    id("java-library")
}

val MINECRAFT_VERSION: String by rootProject.extra
val PARCHMENT_VERSION: String? by rootProject.extra
val NEOFORGE_VERSION: String by rootProject.extra
val SODIUM_DEPENDENCY_NEO: Any by rootProject.extra
val MOD_VERSION: String by rootProject.extra

base {
    archivesName = "iris-neoforge"
}

sourceSets {
}

repositories {
    mavenLocal()
    maven("https://maven.irisshaders.dev/releases")

    maven("https://maven.su5ed.dev/releases")
    maven("https://maven.neoforged.net/releases/")

    maven("https://libraries.minecraft.net/")
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
    maven {
        name = "Maven for PR #2639" // https://github.com/neoforged/NeoForge/pull/2639
        url = uri("https://prmaven.neoforged.net/NeoForge/pr2639")
        content {
            includeModule("net.neoforged", "neoforge")
            includeModule("net.neoforged", "testframework")
        }
    }
}

tasks.jar {

    from(rootDir.resolve("LICENSE.md"))

    filesMatching("neoforge.mods.toml") {
        expand(mapOf("version" to MOD_VERSION))
    }

    manifest.attributes["Main-Class"] = "net.irisshaders.iris.LaunchWarn"
}

tasks.jar.get().destinationDirectory = rootDir.resolve("build").resolve("libs")

neoForge {
    // Specify the version of NeoForge to use.
    interfaceInjectionData.from(file("src/main/resources/interface_injections.json"))

    version = NEOFORGE_VERSION

    if (PARCHMENT_VERSION != null) {
        parchment {
            minecraftVersion = MINECRAFT_VERSION
            mappingsVersion = PARCHMENT_VERSION
        }
    }

    runs {
        create("client") {
            client()
            jvmArgument("-Dneoforge.disableGlValidation=true")
            //environment("LD_PRELOAD", "/usr/lib/librenderdoc.so")
        }
    }

    mods {
        create("iris") {
            sourceSet(sourceSets.main.get())
        }
    }
}

fun includeDep(dependency: String, closure: Action<ExternalModuleDependency>) {
    dependencies.implementation(dependency, closure)
    dependencies.jarJar(dependency, closure)
}

fun includeDep(dependency: String) {
    dependencies.implementation(dependency)
    dependencies.jarJar(dependency)
}

fun includeAdditional(dependency: String) {
    includeDep(dependency)
    dependencies {
        "additionalRuntimeClasspath"(dependency)
    }
}

tasks.named("compileTestJava").configure {
    enabled = false
}
// NeoGradle compiles the game, but we don't want to add our common code to the game's code
val notNeoTask: (Task) -> Boolean = { it: Task -> !it.name.startsWith("neo") && !it.name.startsWith("compileService") }

tasks.withType<JavaCompile>().matching(notNeoTask).configureEach {
    source(project(":common").sourceSets.main.get().allSource)
    source(project(":common").sourceSets.getByName("vendored").allSource)
    source(project(":common").sourceSets.getByName("api").allSource)
    source(project(":common").sourceSets.getByName("desktop").allSource)
}

tasks.withType<Javadoc>().matching(notNeoTask).configureEach {
    source(project(":common").sourceSets.main.get().allJava)
}

tasks.withType<ProcessResources>().matching(notNeoTask).configureEach {
    from(project(":common").sourceSets.main.get().resources)
}

dependencies {
    compileOnly(files(rootDir.resolve("DHApi.jar")))

    compileOnly(project.project(":common").sourceSets.main.get().output)
    compileOnly(project.project(":common").sourceSets.getByName("vendored").output)
    compileOnly(project.project(":common").sourceSets.getByName("headers").output)
    compileOnly(project.project(":common").sourceSets.getByName("api").output)
    runtimeOnly("org.sinytra.forgified-fabric-api:fabric-api-base:0.4.42+d1308ded19")
    runtimeOnly("net.caffeinemc:fabric-renderer-api-v1-neo-test:7.0.0")
   /// runtimeOnly("org.sinytra.forgified-fabric-api:fabric-block-view-api-v2:1.0.10+9afaaf8c19")
   // runtimeOnly("net.caffeinemc:fabric-renderer-api-v1:6.0.0")

    implementation(SODIUM_DEPENDENCY_NEO)
    includeAdditional("io.github.douira:glsl-transformer:3.0.0-pre3")
    includeAdditional("org.anarres:jcpp:1.4.14")
    includeAdditional("org.antlr:antlr4-runtime:4.13.1")
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)
