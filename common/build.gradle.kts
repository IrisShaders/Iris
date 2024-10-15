plugins {
    id("java")
    id("idea")
    id("fabric-loom") version "1.7.3"
    id("com.github.gmazzo.buildconfig") version "5.3.5"
}

repositories {
    maven("https://maven.parchmentmc.org/")

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

val MINECRAFT_VERSION: String by rootProject.extra
val PARCHMENT_VERSION: String? by rootProject.extra
val FABRIC_LOADER_VERSION: String by rootProject.extra
val FABRIC_API_VERSION: String by rootProject.extra

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

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = MINECRAFT_VERSION)

    mappings(loom.layered() {
        officialMojangMappings()
        if (PARCHMENT_VERSION != null) {
            parchment("org.parchmentmc.data:parchment-${MINECRAFT_VERSION}:${PARCHMENT_VERSION}@zip")
        }
    })

    modImplementation("net.fabricmc:fabric-loader:$FABRIC_LOADER_VERSION")

    modCompileOnly("net.fabricmc.fabric-api:fabric-renderer-api-v1:3.2.9+1172e897d7")

    modImplementation("maven.modrinth", "sodium", "mc1.21-0.6.0-beta.2-fabric")
    modCompileOnly("org.antlr:antlr4-runtime:4.13.1")
    modCompileOnly("io.github.douira:glsl-transformer:2.0.1")
    modCompileOnly("org.anarres:jcpp:1.4.14")

    compileOnly(files(rootDir.resolve("DHApi.jar")))
}

sourceSets {
    val main = getByName("main")
    val headers = create("headers")
    val vendored = create("vendored")
    val desktop = getByName("desktop")

    headers.apply {
        java {
            compileClasspath += main.compileClasspath
        }
    }

    vendored.apply {
        java {
            compileClasspath += main.compileClasspath
        }
    }

    desktop.apply {
        java {
            srcDir("src/desktop/java")
        }
    }

    main.apply {
        java {
            compileClasspath += headers.output
            compileClasspath += vendored.output
            runtimeClasspath += vendored.output
        }
    }
}

loom {
    mixin {
        defaultRefmapName = "iris.refmap.json"
        useLegacyMixinAp = true
    }

    accessWidenerPath = file("src/main/resources/iris.accesswidener")

    mods {
        val main by creating { // to match the default mod generated for Forge
            sourceSet("vendored")
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

        val desktop = sourceSets.getByName("desktop")
        from(desktop.output.classesDirs)
        from(desktop.output.resourcesDir)

        manifest.attributes["Main-Class"] = "net.irisshaders.iris.LaunchWarn"
    }
}

// This trick hides common tasks in the IDEA list.
tasks.configureEach {
    group = null
}
