package com.possible_triangle.gradle.features

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.dependencies
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
    mavenLocal()

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

internal fun interface LazyDependencyBuilder {
    fun add(dependency: Any)
}

internal fun Project.lazyDependencies(type: String, block: LazyDependencyBuilder.() -> Unit) {
    dependencies {
        configurations.getByName(type) {
            withDependencies {
                block(LazyDependencyBuilder {
                    this@withDependencies.add(create(it))
                    logger.info("Adding lazy dependency for '{}': {}", type, it)
                })
            }
        }
    }
}