package com.possible_triangle.gradle.features.loaders

import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.features.publishing.modifyPublication
import com.possible_triangle.gradle.features.publishing.removePomDependencies
import com.possible_triangle.gradle.javaVersion
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.stringProperty
import net.minecraftforge.gradle.common.util.MinecraftExtension
import net.minecraftforge.gradle.userdev.UserDevPlugin
import net.minecraftforge.gradle.userdev.jarjar.JarJarProjectExtension
import net.minecraftforge.gradle.userdev.tasks.JarJar
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
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

    fun DependencyHandlerScope.pin(dependency: ModuleDependency): Dependency {
        return add("jarJar", dependency.copy()) {
            jarJar.ranged(this, "[${version},)")
        }
    }

    val includeMixinExtras = project.mixinExtrasVersion?.takeIf { config.mixinsEnabled }?.also {
        dependencies {
            val annotationProcessor = add("annotationProcessor", "io.github.llamalad7:mixinextras-common:${it}")
            add("compileOnly", annotationProcessor!!)
            add("implementation", pin(create("io.github.llamalad7", "mixinextras-forge", it)))
        }
    } != null

    val jarJarEnabled = config.includedLibraries.isNotEmpty() || config.includedMods.isNotEmpty() || includeMixinExtras
    if (jarJarEnabled) jarJar.enable()

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
        if (jarJarEnabled) archiveClassifier.set("slim")
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
                pin(it.get())
            }
        }

        config.includedMods.forEach {
            add("implementation", fg.deobf(it))
            pin(it.get())
        }
    }

    modifyPublication {
        if (jarJarEnabled) {
            artifact(tasks.getByName(UserDevPlugin.JAR_JAR_TASK_NAME))
        }
        removePomDependencies()
    }

    // issues with mixin extras
    tasks.withType<Test> { enabled = false }
    tasks.named("compileTestJava") { enabled = false }

    if (javaVersion <= 17) {
        configurations.all {
            resolutionStrategy {
                force(
                    "org.lwjgl:lwjgl-glfw:3.3.2",
                    "org.lwjgl:lwjgl-jemalloc:3.3.2",
                    "org.lwjgl:lwjgl-openal:3.3.2",
                    "org.lwjgl:lwjgl-opengl:3.3.2",
                    "org.lwjgl:lwjgl-stb:3.3.2",
                    "org.lwjgl:lwjgl-stb:3.3.2",
                    "org.lwjgl:lwjgl:3.3.2"
                )
            }
        }
    }
}