package com.possible_triangle.gradle.features.loaders

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.extra

interface LoaderSpecifics {
    fun addModDependency(
        dependencies: DependencyHandlerScope,
        configuration: String,
        dependencyNotation: ExternalModuleDependency,
        block: ExternalModuleDependency.() -> Unit,
    ): ExternalModuleDependency

    fun addIncluded(
        dependencies: DependencyHandlerScope,
        dependencyNotation: ExternalModuleDependency,
    ): ExternalModuleDependency
}

object TransparentLoaderSpecifics : LoaderSpecifics {
    override fun addModDependency(
        dependencies: DependencyHandlerScope,
        configuration: String,
        dependencyNotation: ExternalModuleDependency,
        block: ExternalModuleDependency.() -> Unit
    ) = dependencies.add("mod${configuration.capitalized()}", dependencyNotation, block)

    override fun addIncluded(
        dependencies: DependencyHandlerScope,
        dependencyNotation: ExternalModuleDependency
    ): ExternalModuleDependency {
        error("it's not supported to include bundled libraries for this loader")
    }
}

private const val LOADER_SPECIFICS_KEY = "loaderSpecifics"

internal fun Project.registerLoaderSpecifics(value: LoaderSpecifics) {
    extra[LOADER_SPECIFICS_KEY] = value
    dependencies.extra[LOADER_SPECIFICS_KEY] = value
}

internal val Project.loaderSpecifics: LoaderSpecifics
    get() = extra[LOADER_SPECIFICS_KEY]?.let { it as? LoaderSpecifics }
        ?: error("only usable when a loader plugin is applied")

private val DependencyHandlerScope.loaderSpecifics: LoaderSpecifics
    get() = extra[LOADER_SPECIFICS_KEY]?.let { it as? LoaderSpecifics }
        ?: error("only usable when a loader plugin is applied")

fun DependencyHandlerScope.addIncluded(
    dependencyNotation: ExternalModuleDependency,
) = loaderSpecifics.addIncluded(this, dependencyNotation)

fun DependencyHandlerScope.addModDependency(
    configuration: String,
    dependencyNotation: ExternalModuleDependency,
    block: ExternalModuleDependency.() -> Unit,
) = loaderSpecifics.addModDependency(this, configuration, dependencyNotation, block)