rootProject.name = "Iris"

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        maven("https://maven.minecraftforge.net/") {
            name = "MinecraftForge"
        }
        maven("https://repo.spongepowered.org/repository/maven-public/") { name = "Sponge Snapshots" }

        mavenCentral()
        gradlePluginPortal()
    }
}

include("common", "neoforge")
