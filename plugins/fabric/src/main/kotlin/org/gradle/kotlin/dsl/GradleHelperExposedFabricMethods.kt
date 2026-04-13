package org.gradle.kotlin.dsl

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency

private fun DependencyHandlerScope.modDependency(
    type: String,
    dependencyNotation: Any,
    block: ModuleDependency.() -> Unit,
): Dependency? {
    val closure = closureOf<ModuleDependency> { block() }
    return dependencies.add(type, dependencyNotation, closure)
}

fun DependencyHandlerScope.modInclude(dependencyNotation: Any, block: ModuleDependency.() -> Unit = {}) {
    modDependency("modApi", dependencyNotation, block)
    modDependency("include", dependencyNotation, block)
}

fun DependencyHandlerScope.apiInclude(dependencyNotation: Any, block: ModuleDependency.() -> Unit = {}) {
    modDependency("api", dependencyNotation, block)
    modDependency("include", dependencyNotation, block)
}