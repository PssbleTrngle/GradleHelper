package com.possible_triangle.gradle.neoforge_api

import com.possible_triangle.gradle.common.GradleHelperCommonPlugin
import com.possible_triangle.gradle.stringProperty
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

class GradleHelperNeoForgeApiPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.apply<GradleHelperCommonPlugin>()

        val neoforgeVersion = target.stringProperty("neoforge_version")
        target.dependencies {
            add("implementation", neoforgeVersion.map { "net.neoforged:neoforge:$it" })
        }
    }
}
