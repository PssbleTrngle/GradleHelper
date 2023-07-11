package com.possible_triangle.gradle

import com.possible_triangle.gradle.features.loaders.isSubProject
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.mod

internal fun Project.configureBaseName() {
    val name = mod.id.flatMap { modId ->
        mod.version.map { modVersion ->
            if (isSubProject) {
                "${modId}-${name.lowercase()}-${modVersion}"
            } else {
                "${modId}-${modVersion}"
            }
        }
    }

    configure<BasePluginExtension> {
        archivesName.set(name)
    }
}
