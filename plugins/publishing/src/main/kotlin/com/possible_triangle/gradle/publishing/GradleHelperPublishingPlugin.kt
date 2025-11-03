package com.possible_triangle.gradle.publishing

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.kotlin.dsl.apply

@Suppress("unused")
class GradleHelperPublishingPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.apply<MavenPublishPlugin>()
    }

}