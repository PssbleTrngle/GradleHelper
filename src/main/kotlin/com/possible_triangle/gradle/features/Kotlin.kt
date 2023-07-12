package com.possible_triangle.gradle.features

import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.repositories
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin

fun Project.enableKotlin() = allprojects {
    apply<KotlinPlatformJvmPlugin>()
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    repositories {
        maven {
            url = uri("https://thedarkcolour.github.io/KotlinForForge/")
            content {
                includeGroup("thedarkcolour")
            }
        }
    }
}

internal fun Project.detectKotlin(): Boolean {
    return plugins.findPlugin(KotlinPlatformJvmPlugin::class.java) != null
}