package com.possible_triangle.gradle.forge

import com.possible_triangle.gradle.features.loaders.mainSourceSet
import com.possible_triangle.gradle.upload.modifyPublication
import org.gradle.api.Project

/**
 * mirror of what is being done in the ModDevGradle plugin to the java component
 * https://github.com/neoforged/ModDevGradle/blob/846b2d70f99519640efd6620e7d6f9034fe38285/src/legacy/java/net/neoforged/moddevgradle/legacyforge/dsl/ObfuscationExtension.java#L122
 */
fun Project.modifyKotlinComponent() {
    for (configurationName in listOf(
        mainSourceSet.runtimeElementsConfigurationName,
        mainSourceSet.apiElementsConfigurationName,
    )) {
        val config = configurations.getByName(configurationName)
        config.artifacts.clear()
    }

    modifyPublication {
        artifact(tasks.reobfJar.archiveFile)
    }
}
