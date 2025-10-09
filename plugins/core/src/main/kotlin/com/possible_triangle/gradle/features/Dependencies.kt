package com.possible_triangle.gradle.features

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.dependencies
import java.net.URI


fun interface LazyDependencyBuilder {
    fun add(dependency: Any)
}

fun Project.lazyDependencies(type: String, block: LazyDependencyBuilder.() -> Unit) {
    dependencies {
        configurations.getByName(type) {
            withDependencies {
                block(LazyDependencyBuilder {
                    this@withDependencies.add(create(it))
                    logger.debug("Adding lazy dependency for '{}': {}", type, it)
                })
            }
        }
    }
}