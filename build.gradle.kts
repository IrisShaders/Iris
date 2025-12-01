
plugins {
    id("java")
    id("fabric-loom") version("1.11.4") apply(false)
}

val MINECRAFT_VERSION by extra { "1.21.9" }
val NEOFORGE_VERSION by extra { "21.9.0-alpha.1.21.9-rc1.20250928.195244" }
val FABRIC_LOADER_VERSION by extra { "0.17.2" }
val FABRIC_API_VERSION by extra { "0.133.12+1.21.9" }

val SODIUM_DEPENDENCY_FABRIC by extra { "maven.modrinth:sodium:IGw7OB6E"}
val SODIUM_DEPENDENCY_NEO by extra { files(rootDir.resolve("custom_sodium").resolve("sodium-neoforge-0.7.0-snapshot+mc1.21.9-rc1-local.jar"))}

// This value can be set to null to disable Parchment.
// TODO: Re-add Parchment
val PARCHMENT_VERSION by extra { null }

// https://semver.org/
val MOD_VERSION by extra { "1.9.6" }

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.jar {
    enabled = false
}

subprojects {
    apply(plugin = "maven-publish")

    java.toolchain.languageVersion = JavaLanguageVersion.of(21)


    fun createVersionString(): String {
        val builder = StringBuilder()

        val isReleaseBuild = project.hasProperty("build.release")
        val buildId = System.getenv("GITHUB_RUN_NUMBER")

        if (isReleaseBuild) {
            builder.append(MOD_VERSION)
        } else {
            builder.append(MOD_VERSION.substringBefore('-'))
            builder.append("-snapshot")
        }

        builder.append("+mc").append(MINECRAFT_VERSION)

        if (!isReleaseBuild) {
            if (buildId != null) {
                builder.append("-build.${buildId}")
            } else {
                builder.append("-local")
            }
        }

        return builder.toString()
    }

    tasks.processResources {
        filesMatching("META-INF/neoforge.mods.toml") {
            expand(mapOf("version" to createVersionString()))
        }
    }

    version = createVersionString()
    group = "net.irisshaders"

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    // Disables Gradle's custom module metadata from being published to maven. The
    // metadata includes mapped dependencies which are not reasonably consumable by
    // other mod developers.
    tasks.withType<GenerateModuleMetadata>().configureEach {
        enabled = false
    }
}
