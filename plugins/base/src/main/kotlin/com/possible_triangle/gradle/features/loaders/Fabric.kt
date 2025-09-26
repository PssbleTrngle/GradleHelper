package com.possible_triangle.gradle.features.loaders

import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.stringProperty
import net.fabricmc.loom.LoomGradlePlugin
import net.fabricmc.loom.LoomRepositoryPlugin
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the

interface FabricExtension : LoaderExtension, OutgoingProjectExtension {
    var apiVersion: String?
    var loaderVersion: String?

    var kotlinFabricVersion: String?

    fun dataGen(factory: DatagenBuilder.() -> Unit = {})

    fun mappings(supplier: LoomGradleExtensionAPI.() -> Dependency)
}

private class FabricExtensionImpl(project: Project) : OutgoingProjectExtensionImpl(project), FabricExtension,
    DatagenBuilder {
    override var loaderVersion: String? = project.stringProperty("fabric_loader_version")
    override var apiVersion: String? = project.stringProperty("fabric_api_version")
    override var kotlinFabricVersion: String? = project.stringProperty("kotlin_fabric_version")

    var enabledDataGen: Boolean = false
        private set

    var mappingsSupplier: LoomGradleExtensionAPI.() -> Dependency = { officialMojangMappings() }
        private set

    override var owner: Project? = project.defaultDataGenProject

    override fun dataGen(factory: DatagenBuilder.() -> Unit) {
        enabledDataGen = true
        factory(this)
    }

    override fun mappings(supplier: LoomGradleExtensionAPI.() -> Dependency) {
        this.mappingsSupplier = supplier
    }
}

private class FixedLoomPlugin : Plugin<Project> {
    private val plugin = LoomGradlePlugin()
    override fun apply(target: Project) {
        plugin.apply(target)
        target.apply<LoomRepositoryPlugin>()
    }
}

fun Project.setupFabric(block: FabricExtension.() -> Unit) {
    apply<FixedLoomPlugin>()

    val config = FabricExtensionImpl(this).apply(block)

    if (config.enabledDataGen) config.requireOwner().configureDatagen()

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
                    runDir("run")

                    property("fabric-api.datagen")
                    property("fabric-api.datagen.output-dir=${config.requireOwner().datagenOutput}")
                    property("fabric-api.datagen.modid=${mod.id.get()}")
                    property("porting_lib.datagen.existing_resources=${existingResources.first()}")
                }
            }
            forEach { run ->
                run.ideConfigGenerated(true)
                run.vmArgs.addAll(JVM_ARGUMENTS)
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

    mainSourceSet.apply {
        config.dependsOn.forEach {
            resources.srcDir(it.mainSourceSet.resources)
        }
    }

    dependencies {
        add("minecraft", mod.minecraftVersion.map { "com.mojang:minecraft:${it}" })
        add("mappings", config.mappingsSupplier(loom))

        lazyDependencies("modImplementation") {
            config.loaderVersion?.let { loaderVersion ->
                add("net.fabricmc:fabric-loader:${loaderVersion}")
            }

            config.apiVersion?.let { apiVersion ->
                add("net.fabricmc.fabric-api:fabric-api:${apiVersion}")
            }

            config.kotlinFabricVersion?.let {
                add("net.fabricmc:fabric-language-kotlin:${it}")
            }

            config.includedMods.forEach {
                add(add("include", it)!!)
            }
        }

        lazyDependencies("implementation") {
            config.dependsOn.forEach {
                add(it)
            }

            config.includedLibraries.forEach {
                add(add("include", it)!!)
            }
        }

    }
}