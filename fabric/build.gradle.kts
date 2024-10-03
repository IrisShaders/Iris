plugins {
    java
    idea
    `maven-publish`
    id("fabric-loom") version("1.8.2")
}

val MINECRAFT_VERSION: String by rootProject.extra
val FABRIC_LOADER_VERSION: String by rootProject.extra
val FABRIC_API_VERSION: String by rootProject.extra
val MOD_VERSION: String by rootProject.extra
val SODIUM_FILE: String by rootProject.extra


base {
    archivesName.set("iris-fabric")
}

sourceSets {
    main.get().apply {
        compileClasspath += project(":common").sourceSets.getByName("headers").output
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${MINECRAFT_VERSION}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.21:2024.07.28@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:$FABRIC_LOADER_VERSION")

    fun addEmbeddedFabricModule(name: String) {
        val module = fabricApi.module(name, FABRIC_API_VERSION)
        modImplementation(module)
        include(module)
    }

    fun addRuntimeFabricModule(name: String) {
        val module = fabricApi.module(name, FABRIC_API_VERSION)
        modRuntimeOnly(module)
    }

    // Fabric API modules
    addEmbeddedFabricModule("fabric-api-base")
    addEmbeddedFabricModule("fabric-key-binding-api-v1")

    modImplementation("org.antlr:antlr4-runtime:4.13.1")
    include("org.antlr:antlr4-runtime:4.13.1")
    modImplementation("io.github.douira:glsl-transformer:2.0.1")
    include("io.github.douira:glsl-transformer:2.0.1")
    modImplementation("org.anarres:jcpp:1.4.14")
    include("org.anarres:jcpp:1.4.14")

    modImplementation(files(rootDir.resolve("custom_sodium").resolve(SODIUM_FILE.replace("LOADER", "fabric"))))

    modCompileOnly(files(rootDir.resolve("DHApi.jar")))

    modRuntimeOnly(group = "com.lodborg", name = "interval-tree", version = "1.0.0")

    addRuntimeFabricModule("fabric-api-base")
    addRuntimeFabricModule("fabric-block-view-api-v2")
    addRuntimeFabricModule("fabric-renderer-api-v1")
    addRuntimeFabricModule("fabric-rendering-data-attachment-v1")
    addRuntimeFabricModule("fabric-rendering-fluids-v1")
    addRuntimeFabricModule("fabric-resource-loader-v0")

    implementation("com.google.code.findbugs:jsr305:3.0.1")
    compileOnly(project(":common"))
}

loom {
    if (project(":common").file("src/main/resources/iris.accesswidener").exists())
        accessWidenerPath.set(project(":common").file("src/main/resources/iris.accesswidener"))

    @Suppress("UnstableApiUsage")
    mixin { defaultRefmapName.set("iris.refmap.json") }

    runs {
        named("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir("run")
            environmentVariable("LD_PRELOAD", "/usr/lib/librenderdoc.so")
            programArg("--tracy")
            vmArgs("-Dmixin.debug.export=true")
        }
        create("clientQuickplay") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            programArgs("--quickPlaySingleplayer", "\"GAMING\"")
            runDir("run")
        }
        named("server") {
            server()
            configName = "Fabric Server"
            ideConfigGenerated(true)
            runDir("run")
        }
    }
}

tasks {
    getByName("compileTestJava") {
        enabled = false
    }

    getByName("test") {
        enabled = false
    }

    withType<JavaCompile> {
        source(project(":common").sourceSets.main.get().allSource)
        source(project(":common").sourceSets.getByName("desktop").allSource)
        source(project(":common").sourceSets.getByName("vendored").allSource)
    }

    javadoc { source(project(":common").sourceSets.main.get().allJava) }

    processResources {
        from(project(":common").sourceSets.main.get().resources)

        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version))
        }
    }

    jar {
        from(rootDir.resolve("LICENSE.md"))

        manifest.attributes["Main-Class"] = "net.irisshaders.iris.LaunchWarn"
    }
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }

    repositories {
        maven("file://${System.getenv("local_maven")}")
    }
}
