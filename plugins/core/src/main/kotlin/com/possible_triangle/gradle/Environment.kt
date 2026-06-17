package com.possible_triangle.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra

class ProjectEnvironment internal constructor(
    private val values: Map<String, String>,
) {
    operator fun get(key: String) = values[key]

    val isCI get() = get("CI") == "true"

    fun toMap() = values.toMap()
}

private fun Project.loadLocalEnv(fileName: String) =
    file(fileName)
        .takeIf { it.exists() }
        ?.readLines()
        ?.filter { it.contains("=") }
        ?.associate {
            val (key, value) = it.split("=")
            key.trim() to value.trim()
        } ?: emptyMap()

private lateinit var loadedEnv: ProjectEnvironment

fun Project.loadEnv(fileName: String = ".env") {
    val localEnv = rootProject.loadLocalEnv(fileName) + loadLocalEnv(fileName)
    val merged = System.getenv() + localEnv
    val filtered = merged.filterValues { it.isNotEmpty() }
    loadedEnv = ProjectEnvironment(filtered)
}

val env get(): ProjectEnvironment = loadedEnv

fun Project.stringProperty(key: String): String? = if (extra.has(key)) extra[key].toString() else null

fun Project.intProperty(key: String): Int? = stringProperty(key)?.toIntOrNull()
