package com.possible_triangle.gradle.publishing

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

private fun Project.isArchitecturyForge(): Boolean {
    try {
        val extension = extensions.getByName("loom")
        val isForge = extension.javaClass.getMethod("isForge")
        return isForge.invoke(extension) as Boolean;
    } catch (ex: Exception) {
        throw RuntimeException("unable to load architectury loom extension", ex)
    }
}

private fun Project.isForge(): Boolean {
    if (plugins.findPlugin("net.minecraftforge.gradle") != null) return true
    if (plugins.findPlugin("dev.architectury.loom") != null) return isArchitecturyForge()
    return false
}

@Suppress("unused")
class GradleHelperPublishingPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.apply<MavenPublishPlugin>()

        target.afterEvaluate {
            configure<PublishingExtension> {
                publications {
                    withType<MavenPublication> {
                        if (target.isForge()) removePomDependencies()
                        else removeRuntimeDependencies()
                    }
                }
            }
        }

        // Disables Gradle's custom module metadata from being published to maven. The
        // metadata includes mapped dependencies which are not reasonably consumable by
        // other mod developers.
        target.tasks.withType<GenerateModuleMetadata> {
            enabled = false
        }
    }

}