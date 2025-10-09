package com.possible_triangle.gradle.repositories

import org.gradle.api.artifacts.dsl.RepositoryHandler
import java.net.URI

private fun RepositoryHandler.modrinthMaven() {
    maven {
        url = URI("https://api.modrinth.com/maven")
        content {
            includeGroup("maven.modrinth")
        }
    }
}

private fun RepositoryHandler.curseMaven() {
    maven {
        url = URI("https://www.cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }
}

internal fun RepositoryHandler.defaultRepositories() {
    mavenCentral()
    mavenLocal()
    modrinthMaven()
    curseMaven()
    mavenFabric()

    maven {
        url = URI("https://libraries.minecraft.net/")
        content {
            includeGroup("com.mojang")
        }
    }

    maven {
        url = URI("https://repo.spongepowered.org/repository/maven-public/")
        content {
            includeGroup("org.spongepowered")
        }
    }
}