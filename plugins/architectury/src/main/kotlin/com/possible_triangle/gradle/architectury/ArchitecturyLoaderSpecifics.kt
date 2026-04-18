package com.possible_triangle.gradle.architectury

import com.possible_triangle.gradle.features.loaders.LoaderSpecifics
import com.possible_triangle.gradle.features.loaders.TransparentLoaderSpecifics
import com.possible_triangle.gradle.features.loaders.appendModPrefix
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.kotlin.dsl.DependencyHandlerScope

object ArchitecturyLoaderSpecifics : LoaderSpecifics {

    override fun addModDependency(
        dependencies: DependencyHandlerScope,
        configuration: String,
        dependencyNotation: ExternalModuleDependency,
        block: ExternalModuleDependency.() -> Unit
    ) = appendModPrefix(dependencies, configuration, dependencyNotation, block)

    override fun addIncluded(
        dependencies: DependencyHandlerScope,
        dependencyNotation: ExternalModuleDependency
    ) = TransparentLoaderSpecifics.addIncluded(dependencies, dependencyNotation)

}