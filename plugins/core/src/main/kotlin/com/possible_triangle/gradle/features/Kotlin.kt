package com.possible_triangle.gradle.features

import com.possible_triangle.gradle.features.loaders.mainSourceSet
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

fun Project.enableKotlin() =
    allprojects {
        apply(plugin = "org.jetbrains.kotlin.jvm")
        apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

        mainSourceSet.java.srcDir("src/main/kotlin")
    }

fun Project.detectKotlin(): Boolean = plugins.findPlugin("org.jetbrains.kotlin.jvm") != null
