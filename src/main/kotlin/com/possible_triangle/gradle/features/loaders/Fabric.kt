package com.possible_triangle.gradle.features.loaders

import com.possible_triangle.gradle.stringProperty
import net.fabricmc.loom.LoomGradlePlugin
import net.fabricmc.loom.LoomRepositoryPlugin
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.kotlin.dsl.*

interface FabricExtension : LoaderExtension, OutgoingProjectExtension {
    var apiVersion: String?
    var loaderVersion: String?

    var kotlinFabricVersion: String?

    fun dataGen()

    fun mappings(supplier: LoomGradleExtensionAPI.() -> Dependency)
}

private class FabricExtensionImpl(project: Project) : OutgoingProjectExtensionImpl(project), FabricExtension {
    override var loaderVersion: String? = project.stringProperty("fabric_loader_version")
    override var apiVersion: String? = project.stringProperty("fabric_api_version")
    override var kotlinFabricVersion: String? = project.stringProperty("kotlin_fabric_version")

    var enabledDataGen: Boolean = false
        private set

    var mappingsSupplier: LoomGradleExtensionAPI.() -> Dependency = { officialMojangMappings() }
        private set

    override fun dataGen() {
        enabledDataGen = true
    }

    override fun mappings(supplier: LoomGradleExtensionAPI.() -> Dependency) {
        this.mappingsSupplier = supplier
    }
}

private class FixedLoomPlugin : Plugin<Project> {
    private val plugin = LoomGradlePlugin()
    override fun apply(target: Project)  {
        plugin.apply(target)
        target.apply<LoomRepositoryPlugin>()
    }
}

fun Project.setupFabric(block: FabricExtension.() -> Unit) {
    apply<FixedLoomPlugin>()

    val config = FabricExtensionImpl(this).apply(block)

    if(config.enabledDataGen) configureDatagen()

    configureOutputProject(config)

    configure<LoomGradleExtensionAPI> {
        if (config.mixinsEnabled) {
            @Suppress("UnstableApiUsage")
            mixin {
                defaultRefmapName.set(mod.id.map { "$it.refmap.json" })
            }
        }

        runs {
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
            if (config.enabledDataGen) {
                create("data") {
                    client()
                    configName = "Fabric Datagen"
                    runDir("run/data")

                    vmArg("-Dfabric-api.datagen")
                    vmArg("-Dfabric-api.datagen.output-dir=${datagenOutput}")
                    vmArg("-Dfabric-api.datagen.modid=${mod.id.get()}")
                }
            }
            forEach {
                it.ideConfigGenerated(true)
            }
        }

        mods {
            create(mod.id.get()) {
                sourceSet(mainSourceSet)
                config.dependsOn.forEach {
                    sourceSet(it.mainSourceSet)
                }
            }
        }
    }

    val loom = the<LoomGradleExtensionAPI>()

    dependencies {
        add("minecraft", mod.minecraftVersion.map { "com.mojang:minecraft:${it}" })
        add("mappings", config.mappingsSupplier(loom))

        val loaderVersion = config.loaderVersion ?: throw IllegalStateException("fabric loader version missing")

        add("modImplementation", "net.fabricmc:fabric-loader:${loaderVersion}")
        config.apiVersion?.let { apiVersion ->
            add("modImplementation", "net.fabricmc.fabric-api:fabric-api:${apiVersion}")
        }

        config.kotlinFabricVersion?.let {
            add("modImplementation", "net.fabricmc:fabric-language-kotlin:${it}")
        }

        config.dependsOn.forEach {
            add("implementation", it)
        }

        config.includes.forEach {
            add("implementation", add("include", it)!!)
        }
    }
}