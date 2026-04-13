package com.possible_triangle.gradle.neoforge

import com.possible_triangle.gradle.features.loaders.LoaderSpecifics
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.add

internal object NeoForgeLoaderSpecifics : LoaderSpecifics {

    override fun addModDependency(
        dependencies: DependencyHandlerScope,
        configuration: String,
        dependencyNotation: ExternalModuleDependency,
        block: ExternalModuleDependency.() -> Unit
    ) = dependencies.add(configuration, dependencyNotation, block)

    override fun addIncluded(
        dependencies: DependencyHandlerScope,
        dependencyNotation: ExternalModuleDependency
    ) = dependencies.add("jarJar", dependencyNotation) {
        version {
            strictly("[${version},)")
            prefer(version!!)
        }
    }
}
