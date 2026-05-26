package com.possible_triangle.gradle.features.loaders

import com.possible_triangle.gradle.features.resolveDependency
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.extra

interface LoaderSpecifics {
    fun addModDependency(
        dependencies: DependencyHandlerScope,
        configuration: String,
        dependencyNotation: Provider<ExternalModuleDependency>,
        closure: Action<ExternalModuleDependency>,
    )

    fun addIncluded(
        dependencies: DependencyHandlerScope,
        dependencyNotation: Provider<ExternalModuleDependency>,
    )
}

object TransparentLoaderSpecifics : LoaderSpecifics {
    override fun addModDependency(
        dependencies: DependencyHandlerScope,
        configuration: String,
        dependencyNotation: Provider<ExternalModuleDependency>,
        closure: Action<ExternalModuleDependency>,
    ) = dependencies.addProvider(configuration, dependencyNotation, closure)

    override fun addIncluded(
        dependencies: DependencyHandlerScope,
        dependencyNotation: Provider<ExternalModuleDependency>,
    ) {
        error("it's not supported to include bundled libraries for this loader")
    }
}

fun addModPrefix(
    dependencies: DependencyHandlerScope,
    configuration: String,
    dependencyNotation: Provider<ExternalModuleDependency>,
    closure: Action<ExternalModuleDependency>,
) = dependencies.addProvider(configuration.addModPrefix(), dependencyNotation, closure)

fun String.addModPrefix() = "mod${capitalized()}"

private const val LOADER_SPECIFICS_KEY = "loaderSpecifics"

internal fun Project.registerLoaderSpecifics(value: LoaderSpecifics) {
    extra[LOADER_SPECIFICS_KEY] = value
    dependencies.extra[LOADER_SPECIFICS_KEY] = value
}

internal val Project.loaderSpecifics: LoaderSpecifics
    get() =
        extra[LOADER_SPECIFICS_KEY]?.let { it as? LoaderSpecifics }
            ?: error("only usable when a loader plugin is applied")

internal val DependencyHandlerScope.loaderSpecifics: LoaderSpecifics
    get() =
        extra[LOADER_SPECIFICS_KEY]?.let { it as? LoaderSpecifics }
            ?: error("only usable when a loader plugin is applied")

fun DependencyHandlerScope.addIncluded(dependencyNotation: Any) = loaderSpecifics.addIncluded(this, resolveDependency(dependencyNotation))

fun DependencyHandlerScope.addModDependency(
    configuration: String,
    dependencyNotation: Any,
    block: ExternalModuleDependency.() -> Unit,
) = loaderSpecifics.addModDependency(this, configuration, resolveDependency(dependencyNotation), block)
