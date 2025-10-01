package com.possible_triangle.gradle.features.loaders

import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderConvertible
import org.gradle.kotlin.dsl.create

interface Included {
    fun include(vararg libraries: String)
    fun include(vararg libraries: ModuleDependency)
    fun include(vararg libraries: Provider<out ModuleDependency>)
    fun include(vararg libraries: ProviderConvertible<out ModuleDependency>)

    fun get(): Collection<Provider<out ModuleDependency>>
}

internal class IncludedImpl(private val project: Project) : Included {
    private fun Collection<String>.asDependencies() = map {
        project.dependencies.create(it) {}
    }

    private val _dependencies = arrayListOf<Provider<out ModuleDependency>>()

    override fun include(vararg libraries: ModuleDependency) {
        _dependencies.addAll(libraries.map { project.provider { it } })
    }

    override fun include(vararg libraries: String) {
        _dependencies.addAll(libraries.toList().asDependencies().map { project.provider { it } })
    }

    override fun include(vararg libraries: ProviderConvertible<out ModuleDependency>) {
        _dependencies.addAll(libraries.map { it.asProvider() })
    }

    override fun include(vararg libraries: Provider<out ModuleDependency>) {
        _dependencies.addAll(libraries)
    }

    override fun get() = _dependencies.toSet()
}