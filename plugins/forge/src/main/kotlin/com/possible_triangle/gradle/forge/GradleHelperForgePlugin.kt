package com.possible_triangle.gradle.forge

import com.possible_triangle.gradle.*
import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.features.loaders.LoaderPlugin
import com.possible_triangle.gradle.features.loaders.LoaderSpecifics
import com.possible_triangle.gradle.features.loaders.ModLoader
import com.possible_triangle.gradle.features.loaders.configureLoaderProject
import com.possible_triangle.gradle.neoforge.configureModRuns
import com.possible_triangle.gradle.neoforge.configureParchment
import com.possible_triangle.gradle.neoforge.sharedConfiguration
import com.possible_triangle.gradle.publishing.removeDependencies
import com.possible_triangle.gradle.upload.UploadExtension
import com.possible_triangle.gradle.upload.modifyPublication
import net.neoforged.moddevgradle.boot.LegacyForgeModDevPlugin
import net.neoforged.moddevgradle.legacyforge.dsl.LegacyForgeExtension
import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*

internal val TaskContainer.reobfJar get() = getByName<Jar>("reobfJar")

class GradleHelperForgePlugin : LoaderPlugin() {
    override fun Project.createSpecifics(): LoaderSpecifics = ForgeLoaderSpecifics

    override fun Project.setup() {
        apply<LegacyForgeModDevPlugin>()

        val config = extensions.create<ForgeExtension, ForgeExtensionImpl>("forge")

        configureLoaderProject(config, ModLoader.FORGE)

        configure<LegacyForgeExtension> {
            version = "${mod.minecraftVersion.get()}-${config.forgeVersion.get()}"

            sharedConfiguration(project)
            configureModRuns(project, ModLoader.FORGE)
            configureParchment(project, config.parchmentMappingsVersion)
        }

        configureModSourceSets()

        configure<UploadExtension> {
            forEach {
                modLoaders.add(ModLoader.FORGE)
                file = tasks.reobfJar.archiveFile
            }
        }

        dependencies {
            lazyDependencies("annotationProcessor") {
                if (config.mixinsEnabled) {
                    add("org.spongepowered:mixin:0.8.7:processor")
                }
            }

            lazyDependencies("api") {
                config.kotlinForgeVersion.orNull?.let {
                    add("thedarkcolour:kotlinforforge:$it")
                }
            }
        }

        tasks.named<Jar>("jar") {
            archiveClassifier = "raw"
        }

        tasks.reobfJar.apply {
            archiveClassifier = ""
        }

        modifyPublication {
            removeDependencies(this)
        }
    }

    override fun Project.finalize() {
        val config = the<ForgeExtension>() as ForgeExtensionImpl

        configureDatagenRun()
        configureMixins()

        config.kotlinForgeVersion.orNull?.let {
            configure<UploadExtension> {
                forEach {
                    if (includeKotlinDependency.get()) dependencies.required("kotlin-for-forge")
                }
            }
        }
    }

    private fun Project.configureModSourceSets() {
        val config = the<ForgeExtension>()
        configure<LegacyForgeExtension> {
            mods.named(mod.id.get()) {
                modSourceSets.addAll(provider(config::modSourceSets))
            }
        }
    }
}
