package com.possible_triangle.gradle.forge

import com.possible_triangle.gradle.*
import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.features.loaders.*
import com.possible_triangle.gradle.upload.UploadExtension
import com.possible_triangle.gradle.upload.modifyPublication
import com.possible_triangle.gradle.upload.removePomDependencies
import net.minecraftforge.gradle.common.util.MinecraftExtension
import net.minecraftforge.gradle.userdev.DependencyManagementExtension
import net.minecraftforge.gradle.userdev.UserDevPlugin
import net.minecraftforge.gradle.userdev.jarjar.JarJarProjectExtension
import net.minecraftforge.gradle.userdev.tasks.JarJar
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*

private val Project.fg get() = the<DependencyManagementExtension>()
internal val Project.jarJar get() = the<JarJarProjectExtension>()

internal fun DependencyHandlerScope.pin(jarJar: JarJarProjectExtension, dependency: ModuleDependency): Dependency {
    return add("jarJar", dependency.copy()) {
        jarJar.ranged(this, "[${version},)")
    }
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
        if (jarJarEnabled) jarJar.enable()

        if (jarJarEnabled) {
            modifyPublication {
                artifact(tasks.getByName(UserDevPlugin.JAR_JAR_TASK_NAME))
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
    }

    private fun Project.configureDatagenRun() {
        val config = the<ForgeExtension>() as ForgeExtensionImpl

        configure<MinecraftExtension> {
            if (config.enabledDataGen) {
                config.requireOwner().configureDatagen()

                runs.named("data") {
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
            } else {
                runs.removeIf { it.name == "data" }
            }
        }
    }

    private fun Project.linkDependencyProjects() {
        val config = the<ForgeExtension>() as ForgeExtensionImpl

        configure<MinecraftExtension> {
            runs.forEach { run ->
                run.mods.named(mod.id.get()) {
                    config.dependsOn.forEach {
                        source(it.mainSourceSet)
                    }
                }
            }
        }
    }

    private fun Project.setupForge() {
        apply<UserDevPlugin>()

        val config = extensions.create<ForgeExtension, ForgeExtensionImpl>("forge")

        val includedMods = IncludedImpl(this, mod.mods) {
            val resolved = fg.deobf(it.get())
            project.provider { resolved as ModuleDependency }
        }

        configure<MinecraftExtension> {
            mappingChannel = config.mappingChannel
            mappingVersion = config.mappingVersion

            val ideaModule = rootProject.name.replace(" ", "_").let { rootName ->
                if (isSubProject) "${rootName}.${project.name}.main"
                else "${rootName}.main"
            }

            runs.create("data")

            runs.create("client") {
                workingDirectory(project.file("run"))
                taskName("Client")
            }

            runs.create("server") {
                workingDirectory(project.file("run/server"))
                taskName("Server")
            }

            runs.forEach { run ->
                run.ideaModule(ideaModule)
                run.property("forge.logging.console.level", "debug")
                run.property("mixin.env.remapRefMap", "true")
                run.property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
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
                    val jarJarTask = tasks.findByPath(UserDevPlugin.JAR_JAR_TASK_NAME) as Jar?
                    (jarJarTask?.takeIf { it.enabled } ?: jarTask).archiveFile.get().asFile
                }
            }
        }

        dependencies {
            add("minecraft", mod.minecraftVersion.flatMap { mcVersion ->
                config.forgeVersion.map { "net.minecraftforge:forge:${mcVersion}-${it}" }
            })

            lazyDependencies("annotationProcessor") {
                if (config.mixinsEnabled) {
                    add("org.spongepowered:mixin:0.8.5:processor")
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

            lazyDependencies("minecraftLibrary") {
                mod.libraries.get().forEach {
                    add(it)
                    pin(jarJar, it)
                }
            }

            lazyDependencies("implementation") {
                includedMods.get().forEach {
                    add(it)
                    pin(jarJar, it)
                }
            }
        }

        modifyPublication {
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
}