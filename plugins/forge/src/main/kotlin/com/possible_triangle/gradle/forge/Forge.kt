package com.possible_triangle.gradle.forge

import com.possible_triangle.gradle.*
import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.features.loaders.*
import com.possible_triangle.gradle.publishing.removePomDependencies
import com.possible_triangle.gradle.upload.UploadExtension
import com.possible_triangle.gradle.upload.modifyPublication
import net.minecraftforge.gradle.MinecraftExtensionForProject
import net.minecraftforge.jarjar.gradle.JarJar
import net.minecraftforge.jarjar.gradle.JarJarExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*

internal val Project.mc get() = the<MinecraftExtensionForProject>()
internal val Project.jarJar get() = the<JarJarExtension>()

internal fun DependencyHandlerScope.pin(jarJar: JarJarExtension, dependency: ModuleDependency): Dependency {
    return add("jarJar", dependency.copy()) {
        // TODO check
    }
}

private fun Project.ideaModule(sourceSet: String = "main") = rootProject.name.replace(" ", "_").let { rootName ->
    if (isSubProject) "${rootName}.${project.name}.$sourceSet"
    else "${rootName}.$sourceSet"
}

class GradleHelperForgePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.apply<GradleHelperCorePlugin>()
        target.setupForge()
        target.afterEvaluate {
            finalize()
            linkDependencyProjects()
        }
    }

    private fun Project.finalize() {
        val config = the<ForgeExtension>() as ForgeExtensionImpl

        configureDatagenRun()
        configureMixins()

        configureOutputProject(config)

        val mixinExtrasIncluded = project.includeMixinExtras()

        val jarJarEnabled = mod.libraries.get().isNotEmpty() || mod.mods.get().isNotEmpty() || mixinExtrasIncluded
        if (jarJarEnabled) {
            jarJar.register {
                archiveClassifier = null
            }
        }

        if (jarJarEnabled) {
            modifyPublication {
                artifact(tasks.getByName("jarJar"))
            }

            tasks.getByName<Jar>("jar") {
                archiveClassifier.set("slim")
            }
        }

        if (jarJarEnabled) {
            tasks.getByName<JarJar>("jarJar") {
                from(mainSourceSet.output)
                config.dependsOn.forEach {
                    from(it.mainSourceSet.output)
                }
            }
        }

        if (config.mixinsEnabled) {
            tasks.withType<Jar> {
                filesMatching("${mod.id.get()}*.mixins.json") {
                    filter(AddMixinRefmap::class, "name" to "${mod.id.get()}.refmap.json")
                }
            }
        }

        config.kotlinForgeVersion.orNull?.let {
            configure<UploadExtension> {
                forEach {
                    if (includeKotlinDependency.get()) dependencies.required("kotlin-for-forge")
                }
            }
        }
    }

    private fun Project.configureDatagenRun() {
        val config = the<ForgeExtension>() as ForgeExtensionImpl

        mc.apply {
            if (config.enabledDataGen) {
                config.requireOwner().configureDatagen()

                runs.named("data") {
                    workingDir.convention(project.layout.projectDirectory.dir("run/data"))
                    // TODO check taskName("Data")

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

                    config.datagenSourceSet.orNull?.let {
                        mods.named(mod.id.get()) {
                            source(it)
                        }

                        ideaModule(project.ideaModule(it.name))
                    }
                }
            } else {
                runs.removeIf { it.name == "data" }
            }
        }
    }

    private fun Project.linkDependencyProjects() {
        val config = the<ForgeExtension>() as ForgeExtensionImpl

        mc.runs.forEach { run ->
            run.mods.named(mod.id.get()) {
                config.dependsOn.forEach {
                    source(it.mainSourceSet)
                }
            }
        }
    }

    private fun Project.setupForge() {
        apply(plugin = "net.minecraftforge.gradle")
        apply(plugin = "net.minecraftforge.renamer")
        apply(plugin = "net.minecraftforge.jarjar")

        val config = extensions.create<ForgeExtension, ForgeExtensionImpl>("forge")

        jarJar.register()

        val includedMods = IncludedImpl(this, mod.mods) {
            val resolved = mc.dependency(it.get())
            resolved.asProvider()
        }

        configure<MinecraftExtensionForProject> {
            mappings(config.mappingChannel.get(), config.mappingVersion.get())

            runs.create("data")

            runs.create("client") {
                workingDir.convention(project.layout.projectDirectory.dir("run"))
                // TODO check taskName("Client")
            }

            runs.create("server") {
                workingDir.convention(project.layout.projectDirectory.dir("run/server"))
                // TODO check taskName("Server")
            }

            runs.forEach { run ->
                // TODO check run.ideaModule(ideaModule())
                run.systemProperty("forge.logging.console.level", "debug")
                run.systemProperty("mixin.env.remapRefMap", "true")
                run.systemProperty("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
                run.jvmArgs.addAll(JVM_ARGUMENTS)
                run.mods.create(mod.id.get()) {
                    source(mainSourceSet)
                }
            }
        }

        configure<UploadExtension> {
            forEach {
                modLoaders.add(ModLoader.FORGE)
                file.set {
                    val jarTask = tasks.getByName<Jar>("jar")
                    val jarJarTask = tasks.findByPath("jarJar") as Jar?
                    (jarJarTask?.takeIf { it.enabled } ?: jarTask).archiveFile.get().asFile
                }
            }
        }

        dependencies {
            lazyDependencies("implementation") {
                add(mc.dependency("net.minecraftforge:forge:${(mod.minecraftVersion.get())}-${(config.forgeVersion.get())}"))
            }

            lazyDependencies("annotationProcessor") {
                if (config.mixinsEnabled) {
                    add("org.spongepowered:mixin:0.8.7:processor")
                }
            }

            lazyDependencies("implementation") {
                config.kotlinForgeVersion.orNull?.let {
                    add("thedarkcolour:kotlinforforge:${it}")
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

                includedMods.get().forEach {
                    add(it)
                    pin(jarJar, it)
                }
            }
        }

        modifyPublication {
            removePomDependencies()
        }

        tasks.withType<Test> { enabled = false }
        tasks.named("compileTestJava") { enabled = false }
        tasks.findByName("compileTestKotlin")?.enabled = false

        if (javaVersion <= 17) {
            // TODO check
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
}