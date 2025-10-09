package com.possible_triangle.gradle.neoforge

import com.possible_triangle.gradle.*
import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.features.loaders.ModLoader
import com.possible_triangle.gradle.features.loaders.configureOutputProject
import com.possible_triangle.gradle.features.loaders.mainSourceSet
import com.possible_triangle.gradle.upload.UploadExtension
import net.neoforged.moddevgradle.boot.ModDevPlugin
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
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

        //tasks.getByName<Jar>("jar") {
        //    if (jarJarEnabled) archiveClassifier.set("slim")
        //}

        configureDatagenRun()
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
                    data()
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
                config.kotlinForgeVersion.orNull?.let {
                    add("thedarkcolour:kotlinforforge-neoforge:${it}")
                }

                config.dependsOn.forEach {
                    add(it)
                }
            }

            lazyDependencies("implementation") {
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