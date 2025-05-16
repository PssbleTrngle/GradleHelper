package com.possible_triangle.gradle.features.loaders

import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.minecraftforge.gradle.userdev.DependencyManagementExtension
import net.neoforged.gradle.userdev.runtime.extension.UserDevRuntimeExtension
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

interface LoaderExtension {
    fun dependOn(vararg projects: Project)
    fun includesLibrary(vararg libraries: String)
}

internal sealed class LoaderExtensionImpl(private val project: Project) : LoaderExtension {
    private val _dependsOn = arrayListOf<Project>()
    private val _includedLibraries = arrayListOf<String>()

    val dependsOn get() = _dependsOn.toSet()
    val includedLibraries get() = _includedLibraries.toSet() + project.mod.includedLibraries.get() + project.rootProject.mod.includedLibraries.get()

    override fun dependOn(vararg projects: Project) {
        _dependsOn.addAll(projects)
    }

    override fun includesLibrary(vararg libraries: String) {
        _includedLibraries.addAll(libraries)
    }
}

internal val Project.mainSourceSet: SourceSet
    get() {
        val sourceSets = the<SourceSetContainer>()
        return sourceSets.getByName("main")
    }

val Project.isSubProject: Boolean get() = rootProject != project

interface OutgoingProjectExtension {
    fun enableMixins()
    fun includesMod(vararg libraries: String)
}

internal sealed class OutgoingProjectExtensionImpl(private val project: Project) : LoaderExtensionImpl(project),
    OutgoingProjectExtension {
    var mixinsEnabled: Boolean = false
        private set

    private val _includedMods = arrayListOf<String>()

    val includedMods get() = _includedMods.toSet() + project.mod.includedMods.get() + project.rootProject.mod.includedMods.get()

    override fun enableMixins() {
        mixinsEnabled = true
    }

    override fun includesMod(vararg libraries: String) {
        _includedMods.addAll(libraries)
    }
}

internal fun Project.configureOutputProject(config: OutgoingProjectExtensionImpl) {
    tasks.getByName<Jar>("jar") {
        from(mainSourceSet.output)
        config.dependsOn.forEach {
            from(it.mainSourceSet.output)
        }
    }

    tasks.withType<JavaCompile> {
        config.dependsOn.forEach {
            source(it.mainSourceSet.allSource)
        }
    }

    tasks.withType<KotlinCompile> {
        config.dependsOn.forEach {
            source(it.mainSourceSet.allSource)
        }
    }
}

internal enum class ModLoader {
    FORGE, FABRIC, NEOFORGE
}

internal fun Project.detectModLoader(): ModLoader? {
    return extensions.findByType<DependencyManagementExtension>()?.let {
        ModLoader.FORGE
    } ?: extensions.findByType<LoomGradleExtensionAPI>()?.let {
        ModLoader.FABRIC
    } ?: extensions.findByType<UserDevRuntimeExtension>()?.let {
        ModLoader.NEOFORGE
    }
}