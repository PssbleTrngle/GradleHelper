package com.possible_triangle.gradle.architectury

import com.possible_triangle.gradle.features.loaders.LoaderSpecifics
import com.possible_triangle.gradle.features.loaders.TransparentLoaderSpecifics
import com.possible_triangle.gradle.features.loaders.appendModPrefix
import org.gradle.api.Action
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.DependencyHandlerScope

object ArchitecturyLoaderSpecifics : LoaderSpecifics {

    override fun addModDependency(
        dependencies: DependencyHandlerScope,
        configuration: String,
        dependencyNotation: Provider<ExternalModuleDependency>,
        closure: Action<ExternalModuleDependency>
    ) = appendModPrefix(dependencies, configuration, dependencyNotation, closure)

    override fun addIncluded(
        dependencies: DependencyHandlerScope,
        dependencyNotation: Provider<ExternalModuleDependency>
    ) = TransparentLoaderSpecifics.addIncluded(dependencies, dependencyNotation)

}