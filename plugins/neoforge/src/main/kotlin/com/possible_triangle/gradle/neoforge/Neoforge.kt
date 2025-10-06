package com.possible_triangle.gradle.neoforge

import com.possible_triangle.gradle.*
import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.features.loaders.ModLoader
import com.possible_triangle.gradle.features.loaders.configureOutputProject
import com.possible_triangle.gradle.features.loaders.mainSourceSet
import com.possible_triangle.gradle.upload.UploadExtension
import net.neoforged.gradle.dsl.common.extensions.JarJar
import net.neoforged.gradle.dsl.common.runs.run.Run
import net.neoforged.gradle.userdev.UserDevPlugin
import net.neoforged.gradle.userdev.UserDevProjectPlugin
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.gradle.language.jvm.tasks.ProcessResources
import net.neoforged.gradle.common.tasks.JarJar as JarJarTask

private val Project.runs get() = extensions.getByName<NamedDomainObjectContainer<Run>>("runs")
private val Project.jarJar get() = the<JarJar>()

fun DependencyHandlerScope.pin(jarJar: JarJar, dependencyNotation: ModuleDependency) {
    add("jarJar", dependencyNotation) {
        jarJar.ranged(this, "[${version},)")
    }
}

class GradleHelperNeoForgePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.apply<GradleHelperCorePlugin>()
        target.setupNeoforge()
        target.afterEvaluate {
            target.finalize()
        }
    }

    private fun Project.finalize() {
        val config = the<NeoforgeExtension>() as NeoforgeExtensionImpl

        val jarJar = the<JarJar>()
        val jarJarEnabled = mod.libraries.get().isNotEmpty() || mod.mods.get().isNotEmpty()
        if (jarJarEnabled) jarJar.enable()

        tasks.getByName<Jar>("jar") {
            if (jarJarEnabled) archiveClassifier.set("slim")
        }

        if (jarJarEnabled) {
            tasks.getByName<JarJarTask>("jarJar") {
                from(mainSourceSet.output)
                config.dependsOn.forEach {
                    from(it.mainSourceSet.output)
                }

                archiveClassifier.set("")
            }
        }

        configureDatagenRun()
    }

    private fun Project.configureDatagenRun() {
        val config = the<NeoforgeExtension>() as NeoforgeExtensionImpl

        if (config.enabledDataGen) {
            config.requireOwner().configureDatagen()

            runs.named("data") {
                workingDirectory(project.file("run/data"))

                val existingResources = existingResources.flatMap { listOf("--existing", it.path) }
                val existingMods = config.existingMods.flatMap { listOf("--existing-mod", it) }
                val dataGenArgs = listOf(
                    "--mod",
                    mod.id.get(),
                    "--all",
                    "--output",
                    config.requireOwner().datagenOutput.path
                ) + existingResources + existingMods

                arguments(dataGenArgs)
            }
        } else {
            runs.removeIf { it.name == "data" }
        }
    }

    private fun Project.setupNeoforge() {
        apply<UserDevPlugin>()

        val config = extensions.create<NeoforgeExtension, NeoforgeExtensionImpl>("neoforge")

        configureOutputProject(config)

        listOf("implementation", "compileOnly", "runtimeOnly").forEach {
            configurations.create("mod${it.capitalized()}") {
                extendsFrom(configurations.getByName(it))
            }
        }

        configure<UploadExtension> {
            forEach {
                val jarTask = tasks.getByName<Jar>("jar")
                val jarJarTask = tasks.findByPath(UserDevProjectPlugin.JAR_JAR_TASK_NAME) as Jar?
                file = (jarJarTask?.takeIf { it.enabled } ?: jarTask).archiveFile
                modLoaders.add(ModLoader.NEOFORGE)
            }
        }

        tasks.withType<ProcessResources> {
            config.dependsOn.forEach {
                from(it.mainSourceSet.resources)
            }
        }

        runs.apply {
            forEach { run ->
                run.jvmArguments.addAll(JVM_ARGUMENTS)
            }

            create("client") {
                workingDirectory(project.file("run"))
            }

            create("server") {
                workingDirectory(project.file("run/server"))
                argument("--nogui")
            }

            create("data")
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
                    pin(jarJar, it)
                }
            }

            mod.mods.get().forEach {
                add("implementation", it)
                pin(jarJar, it)
            }
        }

    }
}