package com.possible_triangle.gradle.publishing

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.withType

class GradleHelperPublishingPluginInternal : Plugin<Project> {

    override fun apply(target: Project) {
        target.apply<MavenPublishPlugin>()

        // target.registerModuleModifyTask()

        // Disables Gradle's custom module metadata from being published to maven. The
        // metadata includes mapped dependencies which are not reasonably consumable by
        // other mod developers.
        target.tasks.withType<GenerateModuleMetadata> {
            enabled = false
        }
    }

}