package com.possible_triangle.gradle

import com.possible_triangle.gradle.features.defaultRepositories
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
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

    val includedLibraries: SetProperty<String>
}

class GradleHelperPlugin : Plugin<Project> {

    override fun apply(target: Project) {

        target.allprojects {
            val rootMod = target.rootProject.takeUnless { it == this }?.extensions?.findByType<ModExtension>()
            val mod = extensions.create<ModExtension>("mod")

            fun <T> configureDefault(default: T?, supplier: ModExtension.() -> Property<T>) {
                mod.supplier().convention(provider { rootMod?.supplier()?.orNull ?: default })
            }

            configureDefault(target.stringProperty("mod_id")) { id }
            configureDefault(target.stringProperty("mod_name")) { name }
            configureDefault(target.stringProperty("mod_version")) { version }
            configureDefault(target.stringProperty("mod_author")) { author }
            configureDefault(target.stringProperty("mc_version")) { minecraftVersion }
            configureDefault(target.stringProperty("release_type")) { releaseType }
            configureDefault(target.stringProperty("repository")) { repository }
            configureDefault(target.stringProperty("maven_group")) { mavenGroup }

            mod.includedLibraries.convention(provider { rootMod?.includedLibraries?.orNull ?: emptySet() })

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
                inputs.property("version", mod.version)

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
                            "mod_version" to mod.version.orNull,
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