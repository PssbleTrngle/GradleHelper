package com.possible_triangle.gradle.forge

import com.possible_triangle.gradle.features.loaders.LoaderSpecifics
import com.possible_triangle.gradle.features.loaders.appendModPrefix
import org.gradle.api.Action
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.DependencyHandlerScope

internal object ForgeLoaderSpecifics : LoaderSpecifics {

    override fun addModDependency(
        dependencies: DependencyHandlerScope,
        configuration: String,
        dependencyNotation: Provider<ExternalModuleDependency>,
        closure: Action<ExternalModuleDependency>
    ) = appendModPrefix(
        dependencies, configuration, dependencyNotation, closure
    )

    override fun addIncluded(
        dependencies: DependencyHandlerScope,
        dependencyNotation: Provider<ExternalModuleDependency>
    ) = dependencies.addProvider("jarJar", dependencyNotation, Action {
        version {
            strictly("[${version},)")
            prefer(version!!)
        }
    })
}
