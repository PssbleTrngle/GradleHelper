package com.possible_triangle.gradle

import com.possible_triangle.gradle.features.loaders.isSubProject
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.kotlin.dsl.configure

internal fun Project.configureBaseName() {
    val name = if (isSubProject) {
        "${mod.id.get()}-${name.lowercase()}-${mod.version.get()}"
    } else {
        "${mod.id.get()}-${mod.version.get()}"
    }

    configure<BasePluginExtension> {
        archivesName.set(name)
    }
}
