package org.gradle.kotlin.dsl

import com.possible_triangle.gradle.features.resolveDependency
import com.possible_triangle.gradle.forge.pin
import groovy.lang.Closure
import org.gradle.api.artifacts.ModuleDependency

fun DependencyHandlerScope.modInclude(dependencyNotation: Any, block: ModuleDependency.() -> Unit = {}) {
    val closure = closureOf<ModuleDependency> { block() } as Closure<Any>
    add("modApi", dependencyNotation, closure)
    pin(resolveDependency(dependencyNotation))
}

fun DependencyHandlerScope.apiInclude(dependencyNotation: Any, block: ModuleDependency.() -> Unit = {}) {
    val closure = closureOf<ModuleDependency> { block() } as Closure<Any>
    add("api", dependencyNotation, closure)
    pin(resolveDependency(dependencyNotation))
}