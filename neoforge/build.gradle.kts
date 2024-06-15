plugins {
    id("idea")
    id("maven-publish")
    id("net.minecraftforge.gradle") version "[6.0,6.2)"
    id("org.spongepowered.mixin") version "0.7-SNAPSHOT"
    id("java-library")
}

base {
    archivesName = "iris-neoforge-1.20.5"
}

val MINECRAFT_VERSION: String by rootProject.extra
val NEOFORGE_VERSION: String by rootProject.extra
val MOD_VERSION: String by rootProject.extra

base {
    archivesName = "iris-neoforge-${MINECRAFT_VERSION}"
}

jarJar.enable()

sourceSets {
    main.get().apply {
        compileClasspath += project(":common").sourceSets.getByName("headers").output
    }
}

repositories {
    flatDir {
        dir(rootDir.resolve("custom_sodium"))
    }
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        //forRepositories(fg.repository) // Only add this if you're using ForgeGradle, otherwise remove this line
        filter {
            includeGroup("maven.modrinth")
        }
    }
}

tasks.build {
}

tasks.jar {
    from(rootDir.resolve("LICENSE.md"))

}

mixin {
    add(sourceSets.main.get(), "iris.refmap.json")
    config("iris.mixins.json")
    config("iris.forge.mixins.json")
}

minecraft {
    mappings("official", MINECRAFT_VERSION)

    copyIdeResources = true //Calls processResources when in dev

    // Automatically enable forge AccessTransformers if the file exists
    // This location is hardcoded in Forge and can not be changed.
    // https://github.com/MinecraftForge/MinecraftForge/blob/be1698bb1554f9c8fa2f58e32b9ab70bc4385e60/fmlloader/src/main/java/net/minecraftforge/fml/loading/moddiscovery/ModFile.java#L123
    val transformerFile = file("src/main/resources/META-INF/accesstransformer.cfg")
    if (transformerFile.exists())
        accessTransformer(transformerFile)

    runs {
        create("client") {
            workingDirectory(project.file("run"))
            ideaModule("${rootProject.name}.${project.name}.main")
            taskName("Client")
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
            mods {
                create("modRun") {
                    source(sourceSets.main.get())
                    source(project(":common").sourceSets.main.get())
                }
            }
        }

        create("server") {
            workingDirectory(project.file("run"))
            ideaModule("${rootProject.name}.${project.name}.main")
            taskName("Server")
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
            mods {
                create("modServerRun") {
                    source(sourceSets.main.get())
                    source(project(":common").sourceSets.main.get())
                }
            }
        }

        create("data") {
            workingDirectory(project.file("run"))
            ideaModule("${rootProject.name}.${project.name}.main")
            args(
                "--mod", "iris",
                "--all",
                "--output", file("src/generated/resources").absolutePath,
                "--existing", file("src/main/resources/").absolutePath
            )
            taskName("Data")
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
            mods {
                create("modDataRun") {
                    source(sourceSets.main.get())
                    source(project(":common").sourceSets.main.get())
                }
            }
        }
    }
}


dependencies {
    minecraft("net.minecraftforge:forge:${MINECRAFT_VERSION}-${NEOFORGE_VERSION}")
    annotationProcessor("org.spongepowered:mixin:0.8.5-SNAPSHOT:processor")
    compileOnly(project(":common"))
    runtimeOnly("com.lodborg:interval-tree:1.0.0")
    implementation("org.antlr:antlr4-runtime:4.13.1")

    implementation("io.github.douira:glsl-transformer:2.0.1")
    jarJar("io.github.douira:glsl-transformer:[2.0.1,2.0.2]") {
        isTransitive = false
    }
    implementation("org.anarres:jcpp:1.4.14")
    jarJar("org.anarres:jcpp:[1.4.14,1.4.15]") {
        isTransitive = false
    }
    implementation(fg.deobf("net.caffeinemc:sodium-forge:0.6.0"))

    compileOnly(files(rootDir.resolve("DHApi.jar")))
    compileOnly("maven.modrinth:immersiveengineering:11mMmtHT")
}

tasks.jarJar {
    archiveClassifier = "jarJar"
}

// NeoGradle compiles the game, but we don't want to add our common code to the game's code
val notNeoTask: (Task) -> Boolean = { it: Task -> !it.name.startsWith("neo") && !it.name.startsWith("compileService") }

tasks.withType<JavaCompile>().matching(notNeoTask).configureEach {
    source(project(":common").sourceSets.main.get().allSource)
    source(project(":common").sourceSets.getByName("sodiumCompatibility").allSource)
    source(project(":common").sourceSets.getByName("vendored").allSource)
}

tasks.withType<Javadoc>().matching(notNeoTask).configureEach {
    source(project(":common").sourceSets.main.get().allJava)
    source(project(":common").sourceSets.getByName("sodiumCompatibility").allJava)
    source(project(":common").sourceSets.getByName("vendored").allJava)
}

tasks.withType<ProcessResources>().matching(notNeoTask).configureEach {
    from(project(":common").sourceSets.main.get().resources)
    from(project(":common").sourceSets.getByName("sodiumCompatibility").resources)
}

java.toolchain.languageVersion = JavaLanguageVersion.of(17)

publishing {
    publications {

    }
    repositories {
        maven {
            url = uri("file://" + System.getenv("local_maven"))
        }
    }
}
