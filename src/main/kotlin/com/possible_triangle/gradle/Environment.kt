package com.possible_triangle.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.jetbrains.kotlin.com.google.common.base.Suppliers

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

val Project.env get(): ProjectEnvironment = Suppliers.memoize { loadEnv() }.get()

internal fun Project.stringProperty(key: String) = if (extra.has(key)) extra[key].toString() else null