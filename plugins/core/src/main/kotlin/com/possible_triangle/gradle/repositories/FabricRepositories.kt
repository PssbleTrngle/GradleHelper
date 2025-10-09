package com.possible_triangle.gradle.repositories

import org.gradle.api.artifacts.dsl.RepositoryHandler
import java.net.URI

fun RepositoryHandler.mavenFabric() {
    maven {
        url = URI("https://maven.fabricmc.net/")
        content {
            includeGroup("net.fabricmc")
            includeGroup("net.fabricmc.fabric-api")
            includeGroup("net.minecraft")
        }
    }
}
