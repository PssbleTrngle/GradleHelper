package com.possible_triangle.gradle.neoforge

import com.possible_triangle.gradle.*
import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.features.loaders.LoaderPlugin
import com.possible_triangle.gradle.features.loaders.LoaderSpecifics
import com.possible_triangle.gradle.features.loaders.ModLoader
import com.possible_triangle.gradle.features.loaders.configureOutputProject
import com.possible_triangle.gradle.features.loaders.mainSourceSet
import com.possible_triangle.gradle.upload.UploadExtension
import net.neoforged.moddevgradle.boot.ModDevPlugin
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.api.Project
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.gradle.language.jvm.tasks.ProcessResources

class GradleHelperNeoForgePlugin : LoaderPlugin() {
    override fun Project.createSpecifics(): LoaderSpecifics = NeoForgeLoaderSpecifics

    override fun Project.setup() {
        apply<ModDevPlugin>()

        val config = extensions.create<NeoforgeExtension, NeoforgeExtensionImpl>("neoforge")

        configure<UploadExtension> {
            forEach {
                val jarTask = tasks.getByName<Jar>("jar")
                file = jarTask.archiveFile
                modLoaders.add(ModLoader.NEOFORGE)
            }
        }

        configure<NeoForgeExtension> {
            validateAccessTransformers = true

            mods.create(mod.id.get()) {
                sourceSet(mainSourceSet)
            }

            runs {
                create("client") {
                    gameDirectory = project.file("run")
                    client()
                }

                create("server") {
                    gameDirectory = project.file("run/server")
                    programArgument("--nogui")
                    server()
                }

                create("data") {
                    if (hasSplitDataRuns()) {
                        clientData()
                    } else {
                        data()
                    }
                }

                forEach { run ->
                    run.jvmArguments.addAll(JVM_ARGUMENTS)
                    run.ideName = "NeoForge ${run.name.capitalized()}"
                }
            }
        }

        dependencies {
            add("implementation", config.neoforgeVersion.map { "net.neoforged:neoforge:$it" })

            lazyDependencies("implementation") {
                config.dependsOn.forEach {
                    add(it)
                }
            }

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
        configureOutputProject(config)
        configureModSourceSets()

        config.kotlinForgeVersion.orNull?.let {
            configure<UploadExtension> {
                forEach {
                    if (includeKotlinDependency.get()) dependencies.required("kotlin-for-forge")
                }
            }
        }

        tasks.withType<ProcessResources> {
            config.dependsOn.forEach {
                from(it.mainSourceSet.resources)
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
