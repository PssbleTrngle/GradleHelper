package com.possible_triangle.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra

class ProjectEnvironment(private val values: Map<String, String>) {
    operator fun get(key: String) = values[key]
    val isCI get() = get("CI") == "true"

    fun toMap() = values.toMap()
}

private fun Project.loadLocalEnv(fileName: String) = file(fileName).takeIf { it.exists() }
    ?.readLines()
    ?.filter { it.contains("=") }
    ?.associate {
        val (key, value) = it.split("=")
        key.trim() to value.trim()
    } ?: emptyMap()

fun Project.loadEnv(fileName: String = ".env"): ProjectEnvironment {
    val localEnv = rootProject.loadLocalEnv(fileName) + loadLocalEnv(fileName)
    return ProjectEnvironment(System.getenv() + localEnv)
}

internal fun Project.stringProperty(key: String): String? = if (extra.has(key)) extra[key].toString() else null

internal fun Project.intProperty(key: String): Int? = stringProperty(key)?.toIntOrNull()
