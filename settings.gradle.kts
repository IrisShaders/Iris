rootProject.name = "Iris"

pluginManagement {
    repositories {
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri("https://maven.neoforged.net/releases/") }
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" // Neo complains if we don't have 21 for some reason, and I can't add 21 to an Action...
}

include("common", "fabric", "neoforge")
