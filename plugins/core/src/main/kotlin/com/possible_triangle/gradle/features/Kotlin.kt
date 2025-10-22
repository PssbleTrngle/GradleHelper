package com.possible_triangle.gradle.features

import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

fun Project.enableKotlin() = allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
}

internal fun Project.detectKotlin(): Boolean {
    return plugins.findPlugin("org.jetbrains.kotlin.jvm") != null
}