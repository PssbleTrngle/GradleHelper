package com.possible_triangle.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.extra

class ProjectEnvironment(private val values: Map<String, String>) {
    operator fun get(key: String) = values[key]
    val isCI get() = get("CI") == "true"
}

fun Project.loadEnv(fileName: String = ".env"): ProjectEnvironment {
    val localEnv = file(fileName).takeIf { it.exists() }?.readLines()?.associate {
        val (key, value) = it.split("=")
        key.trim() to value.trim()
    } ?: emptyMap()

    return ProjectEnvironment(System.getenv() + localEnv)
}

internal fun Project.stringProperty(key: String): String? = if (extra.has(key)) extra[key].toString() else null
internal fun Project.stringProvider(key: String): Provider<String> = provider { stringProperty(key) }
