package com.possible_triangle.gradle.fabric

import com.possible_triangle.gradle.*
import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.features.loaders.LoaderPlugin
import com.possible_triangle.gradle.features.loaders.ModLoader
import com.possible_triangle.gradle.features.loaders.configureOutputProject
import com.possible_triangle.gradle.features.loaders.mainSourceSet
import com.possible_triangle.gradle.upload.UploadExtension
import net.fabricmc.loom.LoomGradlePlugin
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

private val Project.loom get() = the<LoomGradleExtensionAPI>()

class GradleHelperFabricPlugin : LoaderPlugin(FabricLoaderSpecifics) {
    override fun Project.setup() {
        apply<LoomGradlePlugin>()

        val config = extensions.create<FabricExtension, FabricExtensionImpl>("fabric")

        configure<UploadExtension> {
            forEach {
                file = tasks.getByName<RemapJarTask>("remapJar").archiveFile
                modLoaders.add(ModLoader.FABRIC)
            }
        }

        loom.runs {
            named("client") {
                client()
                configName = "Fabric Client"
                runDir("run")
            }

            named("server") {
                server()
                configName = "Fabric Server"
                runDir("run/server")
            }

            create("data")

            forEach { run ->
                run.ideConfigGenerated(true)
                run.vmArgs.addAll(JVM_ARGUMENTS)
            }
        }

        loom.mods {
            create(mod.id.get()) {
                sourceSet(mainSourceSet)
            }
        }

        dependencies {
            add("minecraft", mod.minecraftVersion.map { "com.mojang:minecraft:$it" })
            add(
                "mappings",
                loom.layered {
                    officialMojangMappings()
                    config.parchmentMappingsVersion.orNull?.let {
                        parchment("org.parchmentmc.data:parchment-${mod.minecraftVersion.get()}:$it@zip")
                    }
                },
            )

            lazyDependencies("modImplementation") {
                config.loaderVersion.orNull?.let { loaderVersion ->
                    add("net.fabricmc:fabric-loader:$loaderVersion")
                }

                config.apiVersion.orNull?.let { apiVersion ->
                    add("net.fabricmc.fabric-api:fabric-api:$apiVersion")
                }
            }

            lazyDependencies("modApi") {
                config.kotlinFabricVersion.orNull?.let {
                    add("net.fabricmc:fabric-language-kotlin:$it")
                }
            }

            lazyDependencies("implementation") {
                config.dependsOn.forEach {
                    add(it)
                }
            }
        }
    }

    override fun Project.finalize() {
        configureDatagenRun()
        linkDependencyProjects()
    }

    private fun Project.configureDatagenRun() {
        val config = the<FabricExtension>() as FabricExtensionImpl

        if (config.enabledDataGen) {
            config.requireOwner().configureDatagen()

            loom.runs {
                named("data") {
                    client()
                    configName = "Fabric Datagen"
                    runDir("run/data")

                    property("fabric-api.datagen")
                    property("fabric-api.datagen.output-dir=${config.requireOwner().datagenOutput}")
                    property("fabric-api.datagen.modid=${mod.id.get()}")
                    property("porting_lib.datagen.existing_resources=${existingResources.first()}")
                }
            }
        } else {
            loom.runs.removeIf { it.name == "data" }
        }
    }

    private fun Project.linkDependencyProjects() {
        val config = the<FabricExtension>() as FabricExtensionImpl

        configureOutputProject(config)

        mainSourceSet.apply {
            config.dependsOn.forEach {
                resources.srcDir(it.mainSourceSet.resources)
            }
        }

        loom.mods {
            named(mod.id.get()) {
                config.dependsOn.forEach {
                    sourceSet(it.mainSourceSet)
                }
            }
        }

        config.kotlinFabricVersion.orNull?.let {
            configure<UploadExtension> {
                forEach {
                    if (includeKotlinDependency.get()) dependencies.required("fabric-language-kotlin")
                }
            }
        }
    }
}
