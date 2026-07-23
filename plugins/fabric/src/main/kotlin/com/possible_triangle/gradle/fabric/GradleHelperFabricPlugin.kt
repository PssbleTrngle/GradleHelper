package com.possible_triangle.gradle.fabric

import com.possible_triangle.gradle.JVM_ARGUMENTS
import com.possible_triangle.gradle.SemVer
import com.possible_triangle.gradle.create
import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.features.loaders.LoaderPlugin
import com.possible_triangle.gradle.features.loaders.LoaderSpecifics
import com.possible_triangle.gradle.features.loaders.ModLoader
import com.possible_triangle.gradle.features.loaders.configureLoaderProject
import com.possible_triangle.gradle.features.loaders.mainSourceSet
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.upload.UploadExtension
import net.fabricmc.loom.LoomNoRemapGradlePlugin
import net.fabricmc.loom.LoomRemapGradlePlugin
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*

internal val Project.loom get() = the<LoomGradleExtensionAPI>()

private val Project.useMappings get() = SemVer.parse(mod.minecraftVersion.get()).major == 1

class GradleHelperFabricPlugin : LoaderPlugin() {
    override fun Project.createSpecifics(): LoaderSpecifics = FabricLoaderSpecifics(useMappings)

    override fun Project.setup() {
        if (useMappings) {
            apply<LoomRemapGradlePlugin>()
        } else {
            apply<LoomNoRemapGradlePlugin>()
        }

        val config = extensions.create<FabricExtension, FabricExtensionImpl>("fabric")

        configureLoaderProject(config, ModLoader.FABRIC)

        configure<UploadExtension> {
            forEach {
                file = tasks.getByName<Jar>(if (useMappings) "remapJar" else "jar").archiveFile
                modLoaders.add(ModLoader.FABRIC)
            }
        }

        loom.runs {
            named("client") {
                client()
                displayName = "Fabric Client"
                runDirectory = file("run")
            }

            named("server") {
                server()
                displayName = "Fabric Server"
                runDirectory = file("run/server")
            }

            create("data")

            forEach { run ->
                run.generateRunConfig = true
                run.jvmArguments.addAll(JVM_ARGUMENTS)
            }
        }

        loom.mods {
            create(mod.id.get()) {
                sourceSet(mainSourceSet)
            }
        }

        dependencies {
            add("minecraft", mod.minecraftVersion.map { "com.mojang:minecraft:$it" })

            if (useMappings) {
                add(
                    "mappings",
                    loom.layered {
                        officialMojangMappings()
                        config.parchmentMappingsVersion.orNull?.let {
                            parchment("org.parchmentmc.data:parchment-${mod.minecraftVersion.get()}:$it@zip")
                        }
                    },
                )
            }

            lazyDependencies(if (useMappings) "modImplementation" else "implementation") {
                config.loaderVersion.orNull?.let { loaderVersion ->
                    add("net.fabricmc:fabric-loader:$loaderVersion")
                }

                config.apiVersion.orNull?.let { apiVersion ->
                    add("net.fabricmc.fabric-api:fabric-api:$apiVersion")
                }
            }

            lazyDependencies(if (useMappings) "modApi" else "api") {
                config.kotlinFabricVersion.orNull?.let {
                    add("net.fabricmc:fabric-language-kotlin:$it")
                }
            }
        }
    }

    override fun Project.finalize() {
        configureDatagenRun()
        linkDependencyProjects()
        configureModSourceSets()
    }

    private fun Project.linkDependencyProjects() {
        val config = the<FabricExtension>() as FabricExtensionImpl

        config.kotlinFabricVersion.orNull?.let {
            configure<UploadExtension> {
                forEach {
                    if (includeKotlinDependency.get()) dependencies.required("fabric-language-kotlin")
                }
            }
        }
    }

    private fun Project.configureModSourceSets() {
        val config = the<FabricExtension>()
        loom.mods {
            named(mod.id.get()) {
                config.modSourceSets().forEach {
                    sourceSet(it)
                }
            }
        }
    }
}
