package org.gradle.kotlin.dsl

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency

private fun Project.modDependency(
    type: String,
    dependencyNotation: Any,
    block: ModuleDependency.() -> Unit,
): Dependency? {
    val closure = closureOf<ModuleDependency> { block() }
    return dependencies.add(type, dependencyNotation, closure)
}

fun Project.modApi(dependencyNotation: Any, block: ModuleDependency.() -> Unit = {}) =
    modDependency("api", dependencyNotation, block)

fun Project.modImplementation(dependencyNotation: Any, block: ModuleDependency.() -> Unit = {}) =
    modDependency("implementation", dependencyNotation, block)

 fun Project.modRuntimeOnly(dependencyNotation: Any, block: ModuleDependency.() -> Unit = {}) =
    modDependency("runtimeOnly", dependencyNotation, block)

 fun Project.modCompileOnly(dependencyNotation: Any, block: ModuleDependency.() -> Unit = {}) =
    modDependency("compileOnly", dependencyNotation, block)