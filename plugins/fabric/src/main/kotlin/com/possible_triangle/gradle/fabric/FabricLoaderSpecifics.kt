package com.possible_triangle.gradle.fabric

import com.possible_triangle.gradle.features.loaders.LoaderSpecifics
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.add

internal object FabricLoaderSpecifics : LoaderSpecifics {

    override fun addModDependency(
        dependencies: DependencyHandlerScope,
        configuration: String,
        dependencyNotation: ExternalModuleDependency,
        block: ExternalModuleDependency.() -> Unit
    ) = dependencies.add("mod${configuration.capitalized()}", dependencyNotation, block)

    override fun addIncluded(
        dependencies: DependencyHandlerScope,
        dependencyNotation: ExternalModuleDependency
    ) = dependencies.add("include", dependencyNotation) {}
}
