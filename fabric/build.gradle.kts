import net.fabricmc.loom.task.RunGameTask
import org.gradle.kotlin.dsl.named

plugins {
    id("java")
    id("idea")
    id("net.fabricmc.fabric-loom") version ("1.17.20")
}

evaluationDependsOn(":common")

val MINECRAFT_VERSION: String by rootProject.extra
val PARCHMENT_VERSION: String? by rootProject.extra
val FABRIC_LOADER_VERSION: String by rootProject.extra
val FABRIC_API_VERSION: String by rootProject.extra
val SODIUM_DEPENDENCY_FABRIC: Any by rootProject.extra
val MOD_VERSION: String by rootProject.extra

repositories {
    mavenLocal()
    maven("https://maven.caffeinemc.net/releases")
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
        name = "caffeinemcRepositoryReleases"
        url = uri("https://maven.caffeinemc.net/releases")
    }
}

base {
    archivesName.set("iris-fabric")
}

dependencies {
    minecraft("com.mojang:minecraft:${MINECRAFT_VERSION}")

    implementation("net.fabricmc:fabric-loader:$FABRIC_LOADER_VERSION")

    fun addRuntimeFabricModule(name: String) {
        val module = fabricApi.module(name, FABRIC_API_VERSION)
        runtimeOnly(module)
    }

    fun addCompileFabricModule(name: String) {
        val module = fabricApi.module(name, FABRIC_API_VERSION)
        compileOnly(module)
    }

    fun addEmbeddedFabricModule(name: String) {
        val module = fabricApi.module(name, FABRIC_API_VERSION)
        implementation(module)
        include(module)
    }

    fun implementAndInclude(name: String) {
        implementation(name)
        include(name)
    }

    // Fabric API modules
    addEmbeddedFabricModule("fabric-api-base")
    addEmbeddedFabricModule("fabric-key-mapping-api-v1")
    addRuntimeFabricModule("fabric-block-getter-api-v2")
    addRuntimeFabricModule("fabric-rendering-fluids-v1")
    addRuntimeFabricModule("fabric-resource-loader-v0")
    addRuntimeFabricModule("fabric-lifecycle-events-v1")
    addRuntimeFabricModule("fabric-renderer-api-v1")
    addCompileFabricModule("fabric-renderer-api-v1")

    implementation(SODIUM_DEPENDENCY_FABRIC)
    implementAndInclude("org.antlr:antlr4-runtime:4.13.1")
    implementAndInclude("io.github.douira:glsl-transformer:3.0.0-pre3")
    implementAndInclude("org.anarres:jcpp:1.4.14")

    implementation(project(":common"))
    implementation(project(path = ":common", configuration = "vendoredJar"))
    implementation(project(path = ":common", configuration = "apiJar"))
    compileOnly(project(path = ":common", configuration = "headersJar"))

    compileOnly(files(rootDir.resolve("DHApi.jar")))
}
fun RunGameTask.buildJavaCommand(): Pair<String, String> {
    val javaExe = javaLauncher.get().executablePath.asFile.absolutePath
    val args = buildString {
        allJvmArgs.forEach { append(it).append(' ') }
        append("-cp ").append(classpath.asPath).append(' ')
        append(mainClass.get())
    }
    return javaExe to args
}

fun Exec.inheritRunEnvironment(run: RunGameTask) {
    run.environment.forEach { (k, v) ->
        environment(k, v)
    }
    workingDir = run.workingDir
    standardInput = System.`in`
}
tasks.register<Exec>("nsightGpuTrace") {
    group = "profiling"
    description = "Run under Nsight Graphics GPU Trace Profiler."
    dependsOn("classes")

    val runTask = tasks.named<RunGameTask>("runClient").get()
    val (javaExe, javaArgs) = runTask.buildJavaCommand()


    println(javaExe)
    println("is")
    println(javaArgs)
    inheritRunEnvironment(runTask)

    commandLine(
        "/opt/nsight-graphics/NVIDIA-Nsight-Graphics-2026.3/host/linux-desktop-nomad-x64/ngfx",
        "--activity=GPU Trace Profiler",
        "--exe=$javaExe",
        "--args=$javaArgs",
        "--dir=/mnt/code/Iris/fabric/run",
        "--start-after-hotkey",
        "--real-time-shader-profiler",
        "--multi-pass-metrics",
        "--auto-export",
        "--max-duration-ms=50"
    )
}
tasks.named("compileTestJava").configure {
    enabled = false
}

tasks.named("test").configure {
    enabled = false
}

loom {
    if (project(":common").file("src/main/resources/iris.accesswidener").exists()) {
        accessWidenerPath.set(project(":common").file("src/main/resources/iris.accesswidener"))
    }

    @Suppress("UnstableApiUsage")
    mixin {
        defaultRefmapName.set("iris-fabric.refmap.json")
        useLegacyMixinAp = false
    }

    runs {
        named("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir("run")
            environmentVariable("SDL_VIDEODRIVER", "x11")
            // vmArgs("-Dmixin.debug.export=true")
            // vmArg("-XX:+AllowEnhancedClassRedefinition")
        }
    }
}

tasks {
    processResources {
        from(project.project(":common").sourceSets.main.get().resources)
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version))
        }
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from(zipTree(project.project(":common").tasks.jar.get().archiveFile))

        manifest.attributes["Main-Class"] = "net.irisshaders.iris.LaunchWarn"
    }

    jar.get().destinationDirectory = rootDir.resolve("build").resolve("libs")
}
