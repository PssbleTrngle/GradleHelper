package com.possible_triangle.gradle.features.loaders

import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.stringProperty
import net.minecraftforge.gradle.common.util.MinecraftExtension
import net.minecraftforge.gradle.userdev.UserDevPlugin
import net.minecraftforge.gradle.userdev.jarjar.JarJarProjectExtension
import net.minecraftforge.gradle.userdev.tasks.JarJar
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.spongepowered.asm.gradle.plugins.MixinExtension
import org.spongepowered.asm.gradle.plugins.MixinGradlePlugin

interface ForgeDatagenBuilder : DatagenBuilder {
    fun existing(vararg mods: String)
}

interface ForgeExtension : LoaderExtension, OutgoingProjectExtension {
    var mappingChannel: String
    var mappingVersion: String?
    var forgeVersion: String?

    var kotlinForgeVersion: String?

    fun dataGen(factory: ForgeDatagenBuilder.() -> Unit = {})
}

private class ForgeExtensionImpl(project: Project) : OutgoingProjectExtensionImpl(project),
    ForgeExtension, ForgeDatagenBuilder {
    override var mappingChannel: String = "official"
    override var mappingVersion: String? = null
    override var forgeVersion: String? = project.stringProperty("forge_version")

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

fun Project.setupForge(block: ForgeExtension.() -> Unit) {
    apply<UserDevPlugin>()

    val config = ForgeExtensionImpl(this).apply(block)

    if (config.enabledDataGen) config.requireOwner().configureDatagen()

    configureOutputProject(config)

    val jarJar = the<JarJarProjectExtension>()
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

    configure<MinecraftExtension> {
        mappingChannel.set(config.mappingChannel)
        mappingVersion.set(provider { config.mappingVersion ?: mod.minecraftVersion.get() })

        val ideaModule = rootProject.name.replace(" ", "_").let { rootName ->
            if (isSubProject) "${rootName}.${project.name}.main"
            else "${rootName}.main"
        }

        runs.create("client") {
            workingDirectory(project.file("run"))
            taskName("Client")
        }

        runs.create("server") {
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
                    config.requireOwner().datagenOutput
                ) + existingResources + existingMods

                args(dataGenArgs)
            }
        }

        runs.forEach { run ->
            run.ideaModule(ideaModule)
            run.property("forge.logging.console.level", "debug")
            run.property("mixin.env.remapRefMap", "true")
            run.property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
            run.jvmArgs.addAll(JVM_ARGUMENTS)
            run.mods.create(mod.id.get()) {
                source(mainSourceSet)
                config.dependsOn.forEach {
                    source(it.mainSourceSet)
                }
            }
        }
    }

    tasks.getByName<Jar>("jar") {
        finalizedBy("reobfJar")
        if (jarJarEnabled) archiveClassifier.set("raw")
    }

    if (jarJarEnabled) {
        tasks.getByName<JarJar>("jarJar") {
            from(mainSourceSet.output)
            config.dependsOn.forEach {
                from(it.mainSourceSet.output)
            }

            finalizedBy("reobfJarJar")
            archiveClassifier.set("")
        }
    }

    dependencies {
        val forgeVersion = config.forgeVersion ?: throw IllegalStateException("forge version missing")
        add("minecraft", mod.minecraftVersion.map { "net.minecraftforge:forge:${it}-${forgeVersion}" })

        if (config.mixinsEnabled) {
            add("annotationProcessor", "org.spongepowered:mixin:0.8.5:processor")
        }

        lazyDependencies("implementation") {
            config.kotlinForgeVersion?.let {
                add("thedarkcolour:kotlinforforge:${it}")
            }

            config.dependsOn.forEach {
                add(it)
            }
        }

        lazyDependencies("minecraftLibrary") {
            config.includedLibraries.forEach {
                add(it)
                pin(it)
            }
        }

        config.includedMods.forEach {
            add("implementation", fg.deobf(it))
            pin(it)
        }
    }

}