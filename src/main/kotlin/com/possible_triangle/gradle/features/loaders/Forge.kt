package com.possible_triangle.gradle.features.loaders

import com.possible_triangle.gradle.features.publishing.UploadExtension
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.stringProperty
import net.minecraftforge.gradle.common.util.MinecraftExtension
import net.minecraftforge.gradle.userdev.UserDevPlugin
import net.minecraftforge.gradle.userdev.jarjar.JarJarProjectExtension
import net.minecraftforge.gradle.userdev.tasks.JarJar
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.spongepowered.asm.gradle.plugins.MixinExtension
import org.spongepowered.asm.gradle.plugins.MixinGradlePlugin

interface ForgeExtension : LoaderExtension, OutgoingProjectExtension {
    var mappingChannel: String
    var mappingVersion: String?
    var forgeVersion: String?

    var kotlinForgeVersion: String?

    fun dataGen(existingMods: Collection<String> = emptySet())
}

private class ForgeExtensionImpl(project: Project) : OutgoingProjectExtensionImpl(project, "forge"),
    ForgeExtension {
    override var mappingChannel: String = "official"
    override var mappingVersion: String? = null
    override var forgeVersion: String? = project.stringProperty("forge_version")

    override var kotlinForgeVersion: String? = project.stringProperty("kotlin_forge_version")

    var enabledDataGen: Boolean = false
        private set
    var existingMods: Collection<String> = emptySet()
        private set

    override fun dataGen(existingMods: Collection<String>) {
        enabledDataGen = true
        this.existingMods = existingMods
    }

    override fun UploadExtension.configureCurseforge() {
        if (kotlinForgeVersion != null) dependencies {
            required("kotlin-for-forge")
        }
    }

    override fun UploadExtension.configureModrinth() {
        if (kotlinForgeVersion != null) dependencies {
            required("ordsPcFz")
        }
    }
}

fun Project.forge(block: ForgeExtension.() -> Unit) {
    apply<UserDevPlugin>()

    val config = ForgeExtensionImpl(this).apply(block)

    configureOutputProject(config)

    val jarJar = the<JarJarProjectExtension>()
    if (config.includes.isNotEmpty()) jarJar.enable()

    fun DependencyHandlerScope.include(dependencyNotation: String) {
        add("implementation", dependencyNotation)
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
    }

    configure<MinecraftExtension> {
        mappings(
            config.mappingChannel,
            config.mappingVersion ?: mod.minecraftVersion.get()
        )

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

                val dataGenArgs = listOf(
                    "--mod",
                    mod.id.get(),
                    "--all",
                    "--output",
                    datagenOutput,
                    "--existing",
                    file("src/main/resources")
                ) + config.existingMods.flatMap { listOf("--existing-mod", it) }

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

    dependencies {
        add("compileOnly", "org.spongepowered:mixin:0.8.5")
        add("implementation", "com.google.code.findbugs:jsr305:3.0.2")

        config.dependsOn.forEach {
            add("implementation", it)
        }

        config.includes.forEach {
            include(it)
        }
    }

    tasks.getByName<Jar>("jar") {
        finalizedBy("reobfJar")
    }

    tasks.getByName<JarJar>("jarJar") {
        finalizedBy("reobfJarJar")
        archiveClassifier.set("")
        // TODO works? classifier = ""
    }

    dependencies {
        val forgeVersion = config.forgeVersion ?: throw IllegalArgumentException("forge version missing")
        add("minecraft", "net.minecraftforge:forge:${mod.minecraftVersion.get()}-${forgeVersion}")

        config.dependsOn.forEach {
            add("implementation", it)
        }

        if(config.mixinsEnabled) {
            add("annotationProcessor", "org.spongepowered:mixin:0.8.5:processor")
        }

        config.kotlinForgeVersion?.let {
            add("implementation", "thedarkcolour:kotlinforforge:${it}")
        }
    }

}