package com.possible_triangle.gradle.neoforge

import com.possible_triangle.gradle.*
import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.features.loaders.LoaderPlugin
import com.possible_triangle.gradle.features.loaders.LoaderSpecifics
import com.possible_triangle.gradle.features.loaders.ModLoader
import com.possible_triangle.gradle.features.loaders.configureLoaderProject
import com.possible_triangle.gradle.upload.UploadExtension
import net.neoforged.moddevgradle.boot.ModDevPlugin
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*

class GradleHelperNeoForgePlugin : LoaderPlugin() {
    override fun Project.createSpecifics(): LoaderSpecifics = NeoForgeLoaderSpecifics

    override fun Project.setup() {
        apply<ModDevPlugin>()

        val config = extensions.create<NeoforgeExtension, NeoforgeExtensionImpl>("neoforge")

        configureLoaderProject(config, ModLoader.NEOFORGE)

        configure<UploadExtension> {
            forEach {
                val jarTask = tasks.getByName<Jar>("jar")
                file = jarTask.archiveFile
                modLoaders.add(ModLoader.NEOFORGE)
            }
        }

        configure<NeoForgeExtension> {
            sharedConfiguration(project)
            configureModRuns(project, ModLoader.NEOFORGE)
        }

        dependencies {
            add("implementation", config.neoforgeVersion.map { "net.neoforged:neoforge:$it" })

            lazyDependencies("api") {
                config.kotlinForgeVersion.orNull?.let {
                    add("thedarkcolour:kotlinforforge-neoforge:$it")
                }
            }
        }
    }

    override fun Project.finalize() {
        val config = the<NeoforgeExtension>() as NeoforgeExtensionImpl

        configure<NeoForgeExtension> {
            version = config.neoforgeVersion.get()

            config.parchmentMappingsVersion.orNull?.let {
                parchment {
                    minecraftVersion = mod.minecraftVersion.get()
                    mappingsVersion = it
                }
            }
        }

        setupJUnit()
        configureDatagenRun()
        configureModSourceSets()

        config.kotlinForgeVersion.orNull?.let {
            configure<UploadExtension> {
                forEach {
                    if (includeKotlinDependency.get()) dependencies.required("kotlin-for-forge")
                }
            }
        }
    }

    private fun Project.configureModSourceSets() {
        val config = the<NeoforgeExtension>()
        configure<NeoForgeExtension> {
            mods.named(mod.id.get()) {
                modSourceSets.addAll(provider(config::modSourceSets))
            }
        }
    }

    override fun Project.testEnabled(): Boolean {
        val config = the<NeoforgeExtension>()
        return config.unitTests.get()
    }
}
