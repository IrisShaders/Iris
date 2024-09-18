rootProject.name = "Iris"

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        maven { url = uri("https://maven.neoforged.net/releases/") }

        mavenCentral()
        gradlePluginPortal()

        maven {
            name = "ParchmentMC"
            url = uri("https://maven.parchmentmc.org")
        }
    }
}

include("common", "fabric")
