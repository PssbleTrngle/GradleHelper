package com.possible_triangle.gradle.neoforge

import com.possible_triangle.gradle.*
import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.features.loaders.ModLoader
import com.possible_triangle.gradle.features.loaders.configureOutputProject
import com.possible_triangle.gradle.features.loaders.mainSourceSet
import com.possible_triangle.gradle.upload.UploadExtension
import net.neoforged.moddevgradle.boot.ModDevPlugin
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import net.neoforged.moddevgradle.internal.utils.VersionCapabilitiesInternal
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.tasks.testing.Test
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.gradle.language.jvm.tasks.ProcessResources

fun DependencyHandlerScope.pin(dependencyNotation: ExternalModuleDependency) {
    add("jarJar", dependencyNotation) {
        version {
            strictly("[${version},)")
            prefer(version!!)
        }
    }
}

fun Project.splitDataRuns(): Boolean {
    val version = mod.minecraftVersion.map {
        VersionCapabilitiesInternal.ofMinecraftVersion(it)
    }.getOrElse(
        VersionCapabilitiesInternal.latest()
    )
    return version.splitDataRuns()
}

class GradleHelperNeoForgePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.apply<GradleHelperCorePlugin>()
        target.setupNeoforge()
        target.afterEvaluate {
            finalize()
        }
    }

    private fun Project.finalize() {
        val config = the<NeoforgeExtension>() as NeoforgeExtensionImpl

        configureOutputProject(config)

        configureDatagenRun()

        config.kotlinForgeVersion.orNull?.let {
            configure<UploadExtension> {
                forEach {
                    if (includeKotlinDependency.get()) dependencies.required("kotlin-for-forge")
                }
            }
        }
    }

    private fun Project.configureDatagenRun() {
        val config = the<NeoforgeExtension>() as NeoforgeExtensionImpl

        configure<NeoForgeExtension> {
            version = config.neoforgeVersion.get()

            mods.named(mod.id.get()) {
                config.dependsOn.forEach {
                    sourceSet(it.mainSourceSet)
                }
            }

            if (config.enabledDataGen) {
                config.requireOwner().configureDatagen()

                runs.named("data") {
                    gameDirectory = project.file("run/data")

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
                }
            } else {
                runs.removeIf { it.name == "data" }
            }
        }
    }

    private fun Project.setupNeoforge() {
        apply<ModDevPlugin>()

        val config = extensions.create<NeoforgeExtension, NeoforgeExtensionImpl>("neoforge")

        configure<UploadExtension> {
            forEach {
                val jarTask = tasks.getByName<Jar>("jar")
                file = jarTask.archiveFile
                modLoaders.add(ModLoader.NEOFORGE)
            }
        }

        afterEvaluate {
            tasks.withType<ProcessResources> {
                config.dependsOn.forEach {
                    from(it.mainSourceSet.resources)
                }
            }
        }

        tasks.withType<Test> { enabled = false }
        tasks.named("compileTestJava") { enabled = false }
        tasks.findByName("compileTestKotlin")?.enabled = false

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
                    if (splitDataRuns()) clientData()
                    else data()
                }

                forEach { run ->
                    run.jvmArguments.addAll(JVM_ARGUMENTS)
                    run.ideName = "NeoForge ${run.name.capitalized()}"
                }
            }
        }

        dependencies {
            add("implementation", config.neoforgeVersion.map { "net.neoforged:neoforge:${it}" })

            lazyDependencies("implementation") {
                config.dependsOn.forEach {
                    add(it)
                }
            }

            lazyDependencies("api") {
                config.kotlinForgeVersion.orNull?.let {
                    add("thedarkcolour:kotlinforforge-neoforge:${it}")
                }

                mod.libraries.get().forEach {
                    add(it)
                    pin(it)
                }

                mod.mods.get().forEach {
                    add(it)
                    pin(it)
                }
            }
        }

    }
}