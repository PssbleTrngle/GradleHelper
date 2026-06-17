package com.possible_triangle.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider

class ProjectEnvironment internal constructor(
    private val project: Project,
    private val values: Map<String, String>,
) {
    operator fun get(key: String) = values[key]

    fun provider(key: String) = project.provider { get(key) }

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
    loadedEnv = ProjectEnvironment(project, filtered)
}

val env get(): ProjectEnvironment = loadedEnv

fun Project.stringProperty(key: String): Provider<String> = provider { findProperty(key) as String? }

fun Project.intProperty(key: String): Provider<Int> = stringProperty(key).map { it.toInt() }
