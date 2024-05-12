plugins {
    id("idea")
    id("maven-publish")
    id("net.neoforged.gradle.userdev") version "7.0.105"
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

if (file("src/main/resources/META-INF/accesstransformer.cfg").exists()) {
    minecraft.accessTransformers {
        file("src/main/resources/META-INF/accesstransformer.cfg")
    }
}

jarJar.enable()

sourceSets {
    main.get().apply {
        compileClasspath += project(":common").sourceSets.getByName("headers").output
    }
}

repositories {
    exclusiveContent {
        forRepository {
            maven {
                url = uri("https://maven.pkg.github.com/ims212/forge-frapi")
                credentials {
                    username = "IMS212"
                    // Read only token
                    password = "ghp_" + "DEuGv0Z56vnSOYKLCXdsS9svK4nb9K39C1Hn"
                }
            }
        }
        filter {
            includeGroup("net.caffeinemc.new2")
        }
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

runs {
    configureEach {
        modSource(project.sourceSets.main.get())
    }
    create("client") {
        dependencies {
            runtime("com.lodborg:interval-tree:1.0.0")
            runtime("org.antlr:antlr4-runtime:4.13.1")
            runtime("io.github.douira:glsl-transformer:2.0.1")
            runtime("org.anarres:jcpp:1.4.14")
            //runtime(project(":common").sourceSets.getByName("").output)
        }
    }

    create("data") {
        programArguments.addAll("--mod", "iris", "--all", "--output", file("src/generated/resources/").getAbsolutePath(), "--existing", file("src/main/resources/").getAbsolutePath())
    }
}

dependencies {
    implementation("net.neoforged:neoforge:${NEOFORGE_VERSION}")
    compileOnly(project(":common"))
    runtimeOnly("net.caffeinemc.new2:fabric_api_base:0.4.31")
    runtimeOnly("net.caffeinemc.new2:fabric_renderer_api_v1:3.2.1")
    runtimeOnly("net.caffeinemc.new2:fabric_rendering_data_attachment_v1:0.3.37")
    runtimeOnly("com.lodborg:interval-tree:1.0.0")
    runtimeOnly("net.caffeinemc.new2:fabric_block_view_api_v2:1.0.1")
    implementation("org.antlr:antlr4-runtime:4.13.1")
    implementation("io.github.douira:glsl-transformer:2.0.1")
    implementation("org.anarres:jcpp:1.4.14")
    implementation(files(rootDir.resolve("custom_sodium").resolve("sodium-neoforge-1.20.6-0.6.0-snapshot+mc1.20.6-local-modonly.jar")))

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

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

publishing {
    publications {

    }
    repositories {
        maven {
            url = uri("file://" + System.getenv("local_maven"))
        }
    }
}
