package com.possible_triangle.gradle.features

import groovy.lang.Closure
import org.gradle.api.IllegalDependencyNotation
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.internal.provider.DefaultProvider
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderConvertible
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies

fun interface LazyDependencyBuilder {
    fun add(
        dependency: Any,
        block: ModuleDependency.() -> Unit,
    )

    fun add(dependency: Any) = add(dependency) {}
}

fun Project.lazyDependencies(
    type: String,
    block: LazyDependencyBuilder.() -> Unit,
) {
    dependencies {
        configurations.getByName(type) {
            withDependencies {
                block(
                    LazyDependencyBuilder { notation, block ->
                        val closure = closureOf<ModuleDependency> { block() } as Closure<Any>
                        val dependency = create(notation, closure)
                        this@withDependencies.add(dependency)
                        logger.debug("Adding lazy dependency for '{}': {}", type, notation)
                    },
                )
            }
        }
    }
}

fun DependencyHandlerScope.resolveDependency(dependencyNotation: Any): Provider<ExternalModuleDependency> {
    if (dependencyNotation is ExternalModuleDependency) return DefaultProvider { dependencyNotation }
    if (dependencyNotation is String) return resolveDependency(create(dependencyNotation) {})
    if (dependencyNotation is Provider<*>) {
        return resolveDependency(dependencyNotation.get())
    }
    if (dependencyNotation is ProviderConvertible<*>) {
        return resolveDependency(dependencyNotation.asProvider())
    }

    throw IllegalDependencyNotation("${dependencyNotation::class.qualifiedName} is not a valid dependency notation type")
}
