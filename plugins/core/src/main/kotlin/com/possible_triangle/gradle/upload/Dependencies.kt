package com.possible_triangle.gradle.upload

interface DependencyBuilder {
    fun required(dependency: String)
    fun optional(dependency: String)
    fun embedded(dependency: String)
}

abstract class AbstractDependencyBuilder<T> : DependencyBuilder {
    internal val required = hashSetOf<T>()
    internal val optional = hashSetOf<T>()
    internal val embedded = hashSetOf<T>()

    protected abstract fun resolve(dependency: String): T

    protected fun required(dependency: T) {
        required.add(dependency)
    }

    protected fun optional(dependency: T) {
        optional.add(dependency)
    }

    protected fun embedded(dependency: T) {
        embedded.add(dependency)
    }

    override fun required(dependency: String) = required(resolve(dependency))
    override fun optional(dependency: String) = optional(resolve(dependency))
    override fun embedded(dependency: String) = embedded(resolve(dependency))
}

class SimpleDependencyBuilder : AbstractDependencyBuilder<String>() {
    override fun resolve(dependency: String) = dependency
}