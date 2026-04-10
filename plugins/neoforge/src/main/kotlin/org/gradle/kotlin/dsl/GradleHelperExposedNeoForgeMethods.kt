package org.gradle.kotlin.dsl

import com.possible_triangle.gradle.features.resolveDependency
import com.possible_triangle.gradle.neoforge.pin
import groovy.lang.Closure
import org.gradle.api.Project
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

fun DependencyHandlerScope.modApi(dependencyNotation: Any, block: ModuleDependency.() -> Unit = {}) =
    modDependency("api", dependencyNotation, block)

fun DependencyHandlerScope.modImplementation(dependencyNotation: Any, block: ModuleDependency.() -> Unit = {}) =
    modDependency("implementation", dependencyNotation, block)

fun DependencyHandlerScope.modRuntimeOnly(dependencyNotation: Any, block: ModuleDependency.() -> Unit = {}) =
    modDependency("runtimeOnly", dependencyNotation, block)

fun DependencyHandlerScope.modCompileOnly(dependencyNotation: Any, block: ModuleDependency.() -> Unit = {}) =
    modDependency("compileOnly", dependencyNotation, block)

fun DependencyHandlerScope.modCompileOnlyApi(dependencyNotation: Any, block: ModuleDependency.() -> Unit = {}) =
    modDependency("modCompileOnlyApi", dependencyNotation, block)

fun DependencyHandlerScope.modInclude(dependencyNotation: Any, block: ModuleDependency.() -> Unit = {}) {
    modDependency("api", dependencyNotation, block)
    pin(resolveDependency(dependencyNotation))
}

fun DependencyHandlerScope.apiInclude(dependencyNotation: Any, block: ModuleDependency.() -> Unit = {}) {
    modDependency("api", dependencyNotation, block)
    pin(resolveDependency(dependencyNotation))
}