package com.possible_triangle.gradle.forge

import com.possible_triangle.gradle.*
import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.features.loaders.LoaderPlugin
import com.possible_triangle.gradle.features.loaders.ModLoader
import com.possible_triangle.gradle.features.loaders.configureOutputProject
import com.possible_triangle.gradle.features.loaders.mainSourceSet
import com.possible_triangle.gradle.publishing.removePomDependencies
import com.possible_triangle.gradle.upload.UploadExtension
import com.possible_triangle.gradle.upload.modifyPublication
import net.neoforged.moddevgradle.boot.LegacyForgeModDevPlugin
import net.neoforged.moddevgradle.legacyforge.dsl.LegacyForgeExtension
import org.gradle.api.Project
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.gradle.language.jvm.tasks.ProcessResources

class GradleHelperForgePlugin : LoaderPlugin(ForgeLoaderSpecifics) {

    override fun Project.finalize() {
        val config = the<ForgeExtension>() as ForgeExtensionImpl

        configureDatagenRun()
        configureMixins()

        configureOutputProject(config)

        project.mixinExtrasVersion()?.let {
            includeMixinExtras(it)
        }

        tasks.withType<ProcessResources> {
            config.dependsOn.forEach {
                from(it.mainSourceSet.resources)
            }
        }

        if (config.mixinsEnabled) {
            tasks.withType<Jar> {
                filesMatching("${mod.id.get()}*.mixins.json") {
                    filter(AddMixinRefmap::class, "name" to "${mod.id.get()}.refmap.json")
                }
            }
        }

        config.kotlinForgeVersion.orNull?.let {
            configure<UploadExtension> {
                forEach {
                    if (includeKotlinDependency.get()) dependencies.required("kotlin-for-forge")
                }
            }
        }
    }

    private fun Project.configureModSourceSets(config: ForgeExtensionImpl) {
        configure<LegacyForgeExtension> {
            mods.named(mod.id.get()) {
                modSourceSets.addAll(provider {
                    val dependencies = config.dependsOn.map { it.mainSourceSet }
                    val datagen = listOfNotNull(config.datagenSourceSet.orNull)
                    dependencies + datagen
                })
            }
        }
    }

    private fun Project.configureDatagenRun() {
        val config = the<ForgeExtension>() as ForgeExtensionImpl

        configure<LegacyForgeExtension> {
            if (config.enabledDataGen) {
                config.requireOwner().configureDatagen()

                runs.named("data") {
                    val existingResources = existingResources.flatMap { listOf("--existing", it.path) }
                    val existingMods = config.existingMods.flatMap { listOf("--existing-mod", it) }
                    val dataGenArgs = listOf(
                        "--mod",
                        mod.id.get(),
                        "--all",
                        "--output",
                        config.requireOwner().datagenOutput.path
                    ) + existingResources + existingMods

                    programArguments.addAll(dataGenArgs)

                    config.datagenSourceSet.orNull?.let {
                        sourceSet.set(it)
                    }
                }
            } else {
                runs.removeIf { it.name == "data" }
            }
        }
    }

    override fun Project.setup() {
        apply<LegacyForgeModDevPlugin>()

        val config = extensions.create<ForgeExtension, ForgeExtensionImpl>("forge")

        configure<LegacyForgeExtension> {
            version = "${mod.minecraftVersion.get()}-${config.forgeVersion.get()}"

            validateAccessTransformers = true

            mods.create(mod.id.get()) {
                sourceSet(mainSourceSet)
            }

            config.parchmentMappingsVersion.orNull?.let {
                parchment {
                    minecraftVersion = mod.minecraftVersion.get()
                    mappingsVersion = it
                }
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
                    gameDirectory = project.file("run/data")
                    data()
                }

                forEach { run ->
                    run.jvmArguments.addAll(JVM_ARGUMENTS)
                    run.ideName = "NeoForge ${run.name.capitalized()}"
                }
            }
        }

        configureModSourceSets(config)

        configure<UploadExtension> {
            forEach {
                modLoaders.add(ModLoader.FORGE)
                val jarTask = tasks.getByName<Jar>("jar")
                file.set(jarTask.archiveFile)
            }
        }

        dependencies {
            lazyDependencies("annotationProcessor") {
                if (config.mixinsEnabled) {
                    add("org.spongepowered:mixin:0.8.7:processor")
                }
            }

            lazyDependencies("implementation") {
                config.dependsOn.forEach {
                    add(it)
                }
            }

            lazyDependencies("api") {
                config.kotlinForgeVersion.orNull?.let {
                    add("thedarkcolour:kotlinforforge:${it}")
                }
            }
        }

        modifyPublication {
            removePomDependencies()
        }
    }
}