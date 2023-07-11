package com.possible_triangle.gradle.features

import org.gradle.api.artifacts.dsl.RepositoryHandler
import java.net.URI

fun RepositoryHandler.addModrinthMaven() {
    maven {
        url = URI("https://api.modrinth.com/maven")
        content {
            includeGroup("maven.modrinth")
        }
    }
}

fun RepositoryHandler.addCurseMaven() {
    maven {
        url = URI("https://www.cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }
}

internal fun RepositoryHandler.defaultRepositories() {
    mavenCentral()

    maven {
        url = URI("https://maven.fabricmc.net/")
        content {
            includeGroup("net.fabricmc")
            includeGroup("net.fabricmc.fabric-api")
            includeGroup("net.minecraft")
        }
    }

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