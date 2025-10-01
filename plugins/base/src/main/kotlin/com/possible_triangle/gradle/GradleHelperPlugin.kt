package com.possible_triangle.gradle

import com.possible_triangle.gradle.features.defaultRepositories
import com.possible_triangle.gradle.features.loaders.Included
import com.possible_triangle.gradle.features.loaders.IncludedImpl
import com.possible_triangle.gradle.features.registerHookTasks
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.gradle.language.jvm.tasks.ProcessResources

class GradleHelperPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.registerHookTasks()
        target.allprojects { configure() }
    }

    private fun Project.configure() {
        project.loadEnv()

        val rootMod = rootProject.takeUnless { it == this }?.extensions?.findByType<ModExtension>()
        val mod = extensions.create(ModExtension::class, "mod", ModExtensionImpl::class)

        fun <T : Any> configureDefault(default: T?, supplier: ModExtension.() -> Property<T>) {
            mod.supplier().convention(provider { rootMod?.supplier()?.orNull ?: default })
        }

        val modVersion = env["RELEASE_VERSION"] ?: rootProject.stringProperty("mod_version")
        val mcVersion = rootProject.stringProperty("mc_version") ?: rootProject.stringProperty("minecraft_version")

        configureDefault(rootProject.stringProperty("mod_id")) { id }
        configureDefault(rootProject.stringProperty("mod_name")) { name }
        configureDefault(modVersion) { version }
        configureDefault(rootProject.stringProperty("mod_author")) { author }
        configureDefault(mcVersion) { minecraftVersion }
        configureDefault(rootProject.stringProperty("release_type")) { releaseType }
        configureDefault(rootProject.stringProperty("repository")) { repository }
        configureDefault(rootProject.stringProperty("maven_group")) { mavenGroup }

        repositories {
            defaultRepositories()
        }

        setupJava()
        configureBaseName()
        configureJarTasks()

        @Suppress("UnstableApiUsage")
        tasks.withType<ProcessResources> {
            // this will ensure that this task is redone when the versions change.
            inputs.property("version", mod.version)

            filesMatching(
                listOfNotNull(
                    "META-INF/mods.toml",
                    "META-INF/neoforge.mods.toml",
                    "pack.mcmeta",
                    "fabric.mod.json",
                    mod.id.map { modId -> "${modId}*.mixins.json" }.orNull,
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

        tasks.withType<Jar> {
            exclude(".cache")
            exclude("**/*.bbmodel")
            exclude("**/*.aseprite")
            exclude("**/*.xcf")
        }
    }

}