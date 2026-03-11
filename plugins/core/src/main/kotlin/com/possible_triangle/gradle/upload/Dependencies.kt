package com.possible_triangle.gradle.upload

interface DependencyBuilder {
    fun required(dependency: String)
    fun optional(dependency: String)
    fun embedded(dependency: String)
}

data class DependencyConsumer<T>(
    val required: (T) -> Unit,
    val optional: (T) -> Unit,
    val embedded: (T) -> Unit,
)

abstract class AbstractDependencyBuilder<T> : DependencyBuilder {
    private val required = hashSetOf<T>()
    private val optional = hashSetOf<T>()
    private val embedded = hashSetOf<T>()

    private var consumer: DependencyConsumer<T>? = null

    fun consume(consumer: DependencyConsumer<T>) {
        if (this.consumer != null) error("dependencies have already been consumed")
        this.consumer = consumer
        required.forEach(consumer.required)
        embedded.forEach(consumer.embedded)
        optional.forEach(consumer.optional)
    }

    protected abstract fun resolve(dependency: String): T

    protected fun required(dependency: T) {
        consumer?.required?.invoke(dependency) ?: run {
            required.add(dependency)
        }
    }

    protected fun optional(dependency: T) {
        consumer?.optional?.invoke(dependency) ?: run {
            optional.add(dependency)
        }
    }

    protected fun embedded(dependency: T) {
        consumer?.embedded?.invoke(dependency) ?: run {
            embedded.add(dependency)
        }
    }

    override fun required(dependency: String) = required(resolve(dependency))
    override fun optional(dependency: String) = optional(resolve(dependency))
    override fun embedded(dependency: String) = embedded(resolve(dependency))
}

class SimpleDependencyBuilder : AbstractDependencyBuilder<String>() {
    override fun resolve(dependency: String) = dependency
}