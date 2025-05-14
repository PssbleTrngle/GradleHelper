package com.possible_triangle.gradle.features.loaders

import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.stringProperty
import net.neoforged.gradle.dsl.common.extensions.JarJar
import net.neoforged.gradle.dsl.common.runs.run.Run
import net.neoforged.gradle.userdev.UserDevPlugin
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import net.neoforged.gradle.common.tasks.JarJar as JarJarTask

interface NeoforgeExtension : LoaderExtension, OutgoingProjectExtension {
    var mappingChannel: String
    var mappingVersion: String?
    var neoforgeVersion: String?

    var kotlinForgeVersion: String?

    fun dataGen(factory: ForgeDatagenBuilder.() -> Unit = {})
}

private class NeoforgeExtensionImpl(project: Project) : OutgoingProjectExtensionImpl(project),
    NeoforgeExtension, ForgeDatagenBuilder {
    override var mappingChannel: String = "official"
    override var mappingVersion: String? = null
    override var neoforgeVersion: String? = project.stringProperty("neoforge_version")

    override var kotlinForgeVersion: String? = project.stringProperty("kotlin_forge_version")

    var enabledDataGen: Boolean = false
        private set
    private val _existingMods = mutableSetOf<String>()
    val existingMods: Set<String> get() = _existingMods

    override var owner: Project? = project.defaultDataGenProject

    override fun existing(vararg mods: String) {
        this._existingMods.addAll(mods)
    }

    override fun dataGen(factory: ForgeDatagenBuilder.() -> Unit) {
        enabledDataGen = true
        factory(this)
    }
}

fun Project.setupNeoforge(block: NeoforgeExtension.() -> Unit) {
    apply<UserDevPlugin>()

    val config = NeoforgeExtensionImpl(this).apply(block)

    if (config.enabledDataGen) configureDatagen()

    configureOutputProject(config)

    val jarJar = the<JarJar>()
    val jarJarEnabled = config.includedLibraries.isNotEmpty() || config.includedMods.isNotEmpty()
    if (jarJarEnabled) jarJar.enable()

    fun DependencyHandlerScope.pin(dependencyNotation: String) {
        add("jarJar", dependencyNotation) {
            jarJar.ranged(this, "[${version},)")
        }
    }

    extensions.getByName<NamedDomainObjectContainer<Run>>("runs").apply {
        forEach { run ->
            run.jvmArguments.addAll(JVM_ARGUMENTS)
        }

        create("client") {
            workingDirectory(project.file("run"))
        }

        create("server") {
            workingDirectory(project.file("run/server"))
            programArgument("--nogui")
        }

        if (config.enabledDataGen) {
            create("data") {
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

                programArguments(dataGenArgs)
            }
        }
    }

    tasks.getByName<Jar>("jar") {
        if (jarJarEnabled) archiveClassifier.set("raw")
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

    dependencies {
        val neoforgeVersion = config.neoforgeVersion ?: throw IllegalStateException("neoforge version missing")
        add("implementation", "net.neoforged:neoforge:${neoforgeVersion}")

        lazyDependencies("implementation") {
            config.kotlinForgeVersion?.let {
                add("thedarkcolour:kotlinforforge:${it}")
            }

            config.dependsOn.forEach {
                add(it)
            }
        }

        lazyDependencies("implementation") {
            config.includedLibraries.forEach {
                add(it)
                pin(it)
            }
        }

        config.includedMods.forEach {
            add("implementation", it)
            pin(it)
        }
    }

}