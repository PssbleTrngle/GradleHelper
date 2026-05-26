package com.possible_triangle.gradle.fabric

import com.possible_triangle.gradle.features.loaders.LoaderSpecifics
import com.possible_triangle.gradle.features.loaders.TransparentLoaderSpecifics
import com.possible_triangle.gradle.features.loaders.addModPrefix
import org.gradle.api.Action
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.DependencyHandlerScope

internal class FabricLoaderSpecifics(
    private val useMappings: Boolean,
) : LoaderSpecifics {
    override fun addModDependency(
        dependencies: DependencyHandlerScope,
        configuration: String,
        dependencyNotation: Provider<ExternalModuleDependency>,
        closure: Action<ExternalModuleDependency>,
    ) {
        if (useMappings) {
            addModPrefix(
                dependencies,
                configuration,
                dependencyNotation,
                closure,
            )
        } else {
            return TransparentLoaderSpecifics.addModDependency(
                dependencies,
                configuration,
                dependencyNotation,
                closure,
            )
        }
    }

    override fun addIncluded(
        dependencies: DependencyHandlerScope,
        dependencyNotation: Provider<ExternalModuleDependency>,
    ) = dependencies.addProvider("include", dependencyNotation)
}
