package com.possible_triangle.gradle.features.loaders

import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.minecraftforge.gradle.userdev.DependencyManagementExtension
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

interface LoaderExtension {
    fun dependOn(vararg projects: Project)
    fun includesLibrary(vararg libraries: String)
}

internal sealed class LoaderExtensionImpl(private val project: Project) : LoaderExtension {
    private val parentProject = arrayListOf<Project>()
    private val includedLibraries = arrayListOf<String>()

    val dependsOn get() = parentProject.toSet()
    val includes get() = includedLibraries.toSet() + project.mod.includedLibraries.get() + project.rootProject.mod.includedLibraries.get()

    override fun dependOn(vararg projects: Project) {
        parentProject.addAll(projects)
    }

    override fun includesLibrary(vararg libraries: String) {
        includedLibraries.addAll(libraries)
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
}

internal sealed class OutgoingProjectExtensionImpl(project: Project) : LoaderExtensionImpl(project),
    OutgoingProjectExtension {
    var mixinsEnabled: Boolean = false
        private set

    override fun enableMixins() {
        mixinsEnabled = true
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
    FORGE, FABRIC
}

internal fun Project.detectModLoader(): ModLoader? {
    return extensions.findByType<DependencyManagementExtension>()?.let {
        ModLoader.FORGE
    } ?: extensions.findByType<LoomGradleExtensionAPI>()?.let {
        ModLoader.FABRIC
    }
}