package com.possible_triangle.gradle

import com.modrinth.minotaur.Minotaur
import com.possible_triangle.gradle.repositories.defaultRepositories
import com.possible_triangle.gradle.upload.UploadExtension
import com.possible_triangle.gradle.upload.UploadExtensionImpl
import net.darkhax.curseforgegradle.CurseForgeGradlePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources

class GradleHelperCorePlugin : Plugin<Project> {

    override fun apply(target: Project) = target.configure()

    private fun Project.configure() {
        loadEnv()

        val rootMod = coreProject.takeUnless { it == this }?.extensions?.findByType<ModExtension>()
        val mod = extensions.create<ModExtension, ModExtensionImpl>("mod")

        fun <T : Any> configureDefault(default: T?, supplier: ModExtension.() -> Property<T>) {
            mod.supplier().convention(provider { rootMod?.supplier()?.orNull ?: default })
        }

        val modVersion = env["RELEASE_VERSION"] ?: coreProject.stringProperty("mod_version")
        val mcVersion = coreProject.stringProperty("mc_version") ?: coreProject.stringProperty("minecraft_version")

        configureDefault(coreProject.stringProperty("mod_id")) { id }
        configureDefault(coreProject.stringProperty("mod_name")) { name }
        configureDefault(modVersion) { version }
        configureDefault(coreProject.stringProperty("mod_author")) { author }
        configureDefault(mcVersion) { minecraftVersion }
        configureDefault(coreProject.stringProperty("release_type")) { releaseType }
        configureDefault(coreProject.stringProperty("repository")) { repository }
        configureDefault(coreProject.stringProperty("maven_group")) { mavenGroup }

        repositories {
            defaultRepositories()
        }

        setupJava()
        configureBaseName()
        configureJarTasks()

        configureUpload()

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

    private fun Project.configureUpload() {
        apply<MavenPublishPlugin>()
        apply<CurseForgeGradlePlugin>()
        apply<Minotaur>()

        val upload = extensions.create<UploadExtension, UploadExtensionImpl>("upload")

        project.afterEvaluate {
            upload.setup()
        }
    }

}