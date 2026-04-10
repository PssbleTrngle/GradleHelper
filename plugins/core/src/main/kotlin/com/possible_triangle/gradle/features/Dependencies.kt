package com.possible_triangle.gradle.features

import org.gradle.api.IllegalDependencyNotation
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderConvertible
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.create
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
                    val dep = when(it) {
                        is ProviderConvertible<*> -> it.asProvider().get() as Dependency
                        is Provider<*> -> it.get() as Dependency
                        else -> create(it)
                    }
                    this@withDependencies.add(dep)
                    logger.debug("Adding lazy dependency for '{}': {}", type, it)
                })
            }
        }
    }
}

fun DependencyHandlerScope.resolveDependency(dependencyNotation: Any): ExternalModuleDependency {
    if (dependencyNotation is ExternalModuleDependency) return dependencyNotation.copy()
    if (dependencyNotation is String) return resolveDependency(create(dependencyNotation) {})
    if (dependencyNotation is Provider<*>)
        return resolveDependency((dependencyNotation as Provider<ExternalModuleDependency>).get())
    if (dependencyNotation is ProviderConvertible<*>)
        return resolveDependency(dependencyNotation.asProvider())

    throw IllegalDependencyNotation("${dependencyNotation::class.qualifiedName} is not a valid dependency notation type")
}