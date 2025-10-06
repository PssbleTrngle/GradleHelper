package com.possible_triangle.gradle.features.loaders

import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderConvertible
import org.gradle.kotlin.dsl.create

typealias OnAdd = (Collection<Provider<out ModuleDependency>>) -> Unit

interface Included {
    fun include(vararg libraries: String)
    fun include(vararg libraries: ModuleDependency)
    fun include(vararg libraries: Provider<out ModuleDependency>)
    fun include(vararg libraries: ProviderConvertible<out ModuleDependency>)

    fun get(): Collection<ModuleDependency>

    fun onAdded(callback: OnAdd): Unit
}

class IncludedImpl(
    private val project: Project,
    parent: Included?,
    private val transformer: (Provider<out ModuleDependency>) -> Provider<out ModuleDependency> = { it }
) : Included {

    private val _dependencies = arrayListOf<Provider<out ModuleDependency>>()
    private val listeners = hashSetOf<OnAdd>()

    init {
        parent?.onAdded(::include)
    }

    fun Collection<String>.asDependencies() = map {
        project.dependencies.create(it) {}
    }

    private fun include(dependencies: Collection<Provider<out ModuleDependency>>) {
        _dependencies.addAll(dependencies.map(transformer))
        listeners.forEach {
            it(dependencies)
        }
    }

    override fun include(vararg libraries: ModuleDependency) {
        include(libraries.map { project.provider { it } })
    }

    override fun include(vararg libraries: String) {
        include(libraries.toList().asDependencies().map { project.provider { it } })
    }

    override fun include(vararg libraries: ProviderConvertible<out ModuleDependency>) {
        include(libraries.map { it.asProvider() })
    }

    override fun include(vararg libraries: Provider<out ModuleDependency>) {
        include(libraries.toList())
    }

    override fun get(): Collection<ModuleDependency> {
        return _dependencies.mapTo(hashSetOf()) { it.get().copy() }
    }

    override fun onAdded(callback: OnAdd) {
        callback(_dependencies)
        listeners.add(callback)
    }

}