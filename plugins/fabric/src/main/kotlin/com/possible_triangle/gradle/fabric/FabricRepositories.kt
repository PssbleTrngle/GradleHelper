package com.possible_triangle.gradle.fabric

import org.gradle.api.artifacts.dsl.RepositoryHandler
import java.net.URI


internal fun RepositoryHandler.fabricRepositories() {
    maven {
        url = URI("https://maven.fabricmc.net/")
        content {
            includeGroup("net.fabricmc")
            includeGroup("net.fabricmc.fabric-api")
            includeGroup("net.minecraft")
        }
    }
}
