package com.possible_triangle.gradle.features.loaders

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.kotlin.dsl.DependencyHandlerScope
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

private const val LOADER_SPECIFICS_KEY = "loaderSpecifics"

fun registerLoaderSpecifics(project: Project, value: LoaderSpecifics) {
    project.extra[LOADER_SPECIFICS_KEY] = value
    project.dependencies.extra[LOADER_SPECIFICS_KEY] = value
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