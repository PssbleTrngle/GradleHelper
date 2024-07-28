package com.possible_triangle.gradle.features.loaders

import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.stringProperty
import net.neoforged.gradle.dsl.common.extensions.JarJar
import net.neoforged.gradle.userdev.UserDevPlugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.spongepowered.asm.gradle.plugins.MixinExtension
import org.spongepowered.asm.gradle.plugins.MixinGradlePlugin
import net.neoforged.gradle.common.tasks.JarJar as JarJarTask

interface NeoforgeExtension : LoaderExtension, OutgoingProjectExtension {
    var mappingChannel: String
    var mappingVersion: String?
    var neoforgeVersion: String?

    var kotlinForgeVersion: String?

    fun dataGen(existingMods: Collection<String> = emptySet())
}

private class NeoforgeExtensionImpl(project: Project) : OutgoingProjectExtensionImpl(project),
    NeoforgeExtension {
    override var mappingChannel: String = "official"
    override var mappingVersion: String? = null
    override var neoforgeVersion: String? = project.stringProperty("forge_version")

    override var kotlinForgeVersion: String? = project.stringProperty("kotlin_forge_version")

    var enabledDataGen: Boolean = false
        private set
    var existingMods: Collection<String> = emptySet()
        private set

    override fun dataGen(existingMods: Collection<String>) {
        enabledDataGen = true
        this.existingMods = existingMods
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

    if (config.mixinsEnabled) {
        apply<MixinGradlePlugin>()
        configure<MixinExtension> {
            add(mainSourceSet, "${mod.id.get()}.refmap.json")
            config("${mod.id.get()}.mixins.json")
        }

        // workaround because of https://github.com/SpongePowered/MixinGradle/issues/48
        tasks.withType<JavaCompile> {
            doFirst {
                options.compilerArgs.replaceAll { it: Any ->
                    it.toString()
                }
            }
        }
    }

    /*
    configure<RunsExtension> {
        val ideaModule = rootProject.name.replace(" ", "_").let { rootName ->
            if (isSubProject) "${rootName}.${project.name}.main"
            else "${rootName}.main"
        }

        create("client") {
            workingDirectory(project.file("run"))
            taskName("Client")
        }

        create("server") {
            workingDirectory(project.file("run/server"))
            taskName("Server")
        }

        if (config.enabledDataGen) {
            runs.create("data") {
                workingDirectory(project.file("run/data"))
                taskName("Data")

                val existingResources = existingResources.flatMap { listOf("--existing", it) }
                val existingMods = config.existingMods.flatMap { listOf("--existing-mod", it) }
                val dataGenArgs = listOf(
                    "--mod",
                    mod.id.get(),
                    "--all",
                    "--output",
                    datagenOutput
                ) + existingResources + existingMods

                args(dataGenArgs)
            }
        }

        runs.forEach { runConfig ->
            runConfig.ideaModule(ideaModule)
            runConfig.property("forge.logging.console.level", "debug")
            runConfig.property("mixin.env.remapRefMap", "true")
            runConfig.property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
            runConfig.mods.create(mod.id.get()) {
                source(mainSourceSet)
                config.dependsOn.forEach {
                    source(it.mainSourceSet)
                }
            }
        }
    }
    */

    tasks.getByName<Jar>("jar") {
        finalizedBy("reobfJar")
        if (jarJarEnabled) archiveClassifier.set("raw")
    }

    if (jarJarEnabled) {
        tasks.getByName<JarJarTask>("jarJar") {
            from(mainSourceSet.output)
            config.dependsOn.forEach {
                from(it.mainSourceSet.output)
            }

            finalizedBy("reobfJarJar")
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