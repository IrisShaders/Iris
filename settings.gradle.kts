rootProject.name = "Iris"

pluginManagement {
    repositories {
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri("https://maven.neoforged.net/releases/") }
        gradlePluginPortal()
    }

    val fabric_loom_version: String by settings
    plugins {
        id("fabric-loom") version(fabric_loom_version) apply(false)
    }
}

include("common", "fabric", "neoforge")
