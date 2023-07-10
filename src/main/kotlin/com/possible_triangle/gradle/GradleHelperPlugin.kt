package com.possible_triangle.gradle

import com.possible_triangle.gradle.features.defaultRepositories
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.kotlin.dsl.*
import org.gradle.language.jvm.tasks.ProcessResources

interface ModExtension {
    val id: Property<String>
    val name: Property<String>
    val version: Property<String>
    val author: Property<String>
    val minecraftVersion: Property<String>
    val releaseType: Property<String>
    val repository: Property<String>
    val mavenGroup: Property<String>

    val includedLibraries: Property<Collection<String>>
}

fun Project.mod(block: ModExtension.() -> Unit) = extensions.configure<ModExtension>(block)

val Project.mod get() = rootProject.extensions.getByName<ModExtension>("mod")

class GradleHelperPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val mod = target.extensions.create<ModExtension>("mod")

        mod.id.convention(target.stringProperty("mod_id"))
        mod.name.convention(target.stringProperty("mod_name"))
        mod.version.convention(target.stringProperty("mod_version"))
        mod.author.convention(target.stringProperty("mod_author"))
        mod.minecraftVersion.convention(target.stringProperty("mc_version"))
        mod.releaseType.convention(target.stringProperty("release_type"))
        mod.repository.convention(target.stringProperty("repository"))
        mod.mavenGroup.convention(target.stringProperty("maven_group"))

        mod.includedLibraries.convention(emptySet())

        target.allprojects {
            repositories {
                defaultRepositories()
            }

            setupJava()
            configureBaseName()
        }

        target.subprojects {
            configureJarTasks()

            @Suppress("UnstableApiUsage")
            tasks.withType<ProcessResources> {
                // this will ensure that this task is redone when the versions change.
                inputs.property("version", mod.version.get())

                filesMatching(
                    listOf(
                        "META-INF/mods.toml",
                        "pack.mcmeta",
                        "fabric.mod.json",
                        "${mod.id.get()}.mixins.json"
                    )
                ) {
                    expand(
                        mapOf(
                            "version" to mod.version.orNull,
                            "mod_name" to mod.name.orNull,
                            "mod_id" to mod.id.orNull,
                            "mod_author" to mod.author.orNull,
                            "repository" to mod.repository.orNull,
                        ).filterValues { it != null }
                    )
                }
            }

            // Disables Gradle's custom module metadata from being published to maven. The
            // metadata includes mapped dependencies which are not reasonably consumable by
            // other mod developers.
            tasks.withType<GenerateModuleMetadata> {
                enabled = false
            }
        }
    }

}