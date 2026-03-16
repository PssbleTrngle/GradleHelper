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
) {
    companion object {
        fun <T> empty() = DependencyConsumer<T>(
            required = {},
            optional = {},
            embedded = {},
        )
    }

    operator fun plus(other: DependencyConsumer<T>) = DependencyConsumer<T>(
        required = {
            this.required(it)
            other.required(it)
        },
        optional = {
            this.optional(it)
            other.optional(it)
        },
        embedded = {
            this.embedded(it)
            other.embedded(it)
        },
    )
}

abstract class AbstractDependencyBuilder<T> : DependencyBuilder {
    private val required = hashSetOf<T>()
    private val optional = hashSetOf<T>()
    private val embedded = hashSetOf<T>()

    private var consumer: DependencyConsumer<T> = DependencyConsumer.empty()

    fun consume(consumer: DependencyConsumer<T>) {
        this.consumer += consumer
        required.forEach(consumer.required)
        embedded.forEach(consumer.embedded)
        optional.forEach(consumer.optional)
    }

    protected abstract fun resolve(dependency: String): T

    protected fun required(dependency: T) {
        if (required.add(dependency)) {
            consumer.required(dependency)
        }
    }

    protected fun optional(dependency: T) {
        if (optional.add(dependency)) {
            consumer.optional(dependency)
        }
    }

    protected fun embedded(dependency: T) {
        if (embedded.add(dependency)) {
            consumer.embedded(dependency)
        }
    }

    override fun required(dependency: String) = required(resolve(dependency))
    override fun optional(dependency: String) = optional(resolve(dependency))
    override fun embedded(dependency: String) = embedded(resolve(dependency))
}

class SimpleDependencyBuilder : AbstractDependencyBuilder<String>() {
    override fun resolve(dependency: String) = dependency
}