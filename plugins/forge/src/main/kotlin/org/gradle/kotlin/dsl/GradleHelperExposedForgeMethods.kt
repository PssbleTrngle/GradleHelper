package org.gradle.kotlin.dsl

import net.minecraftforge.gradle.userdev.DependencyManagementExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderConvertible

private val Project.fg get() = the<DependencyManagementExtension>()

private fun Any.resolveDependency() = when (this) {
    is Provider<*> -> get()
    is ProviderConvertible<*> -> asProvider().get()
    else -> this
}

private fun Project.modDependency(
    type: String,
    dependencyNotation: Any,
    block: ModuleDependency.() -> Unit,
): Dependency? {
    val closure = closureOf<ModuleDependency> { block() }
    return dependencies.add(type, fg.deobf(dependencyNotation.resolveDependency(), closure))
}

fun Project.modApi(dependencyNotation: Any, block: ModuleDependency.() -> Unit = {}) =
    modDependency("api", dependencyNotation, block)

fun Project.modImplementation(dependencyNotation: Any, block: ModuleDependency.() -> Unit = {}) =
    modDependency("implementation", dependencyNotation, block)

 fun Project.modRuntimeOnly(dependencyNotation: Any, block: ModuleDependency.() -> Unit = {}) =
    modDependency("runtimeOnly", dependencyNotation, block)

 fun Project.modCompileOnly(dependencyNotation: Any, block: ModuleDependency.() -> Unit = {}) =
    modDependency("compileOnly", dependencyNotation, block)