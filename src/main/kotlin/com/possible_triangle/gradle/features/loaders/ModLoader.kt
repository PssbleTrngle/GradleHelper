package com.possible_triangle.gradle.features.loaders

import com.possible_triangle.gradle.features.publishing.UploadExtension
import com.possible_triangle.gradle.features.publishing.uploadToCurseforge
import com.possible_triangle.gradle.features.publishing.uploadToModrinth
import com.possible_triangle.gradle.mod
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

interface LoaderExtension {
    fun dependOn(vararg projects: Project)
    fun includesLibrary(vararg libraries: String)
}

internal sealed class LoaderExtensionImpl(private val project: Project) : LoaderExtension {
    private val parentProject = arrayListOf<Project>()
    private val includedLibraries = arrayListOf<String>()

    val dependsOn get() = parentProject.toSet()
    val includes get() = includedLibraries.toSet() + project.mod.includedLibraries.get()

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
    fun uploadToCurseforge(block: UploadExtension.() -> Unit)
    fun uploadToModrinth(block: UploadExtension.() -> Unit)
    fun enableMixins()
}

internal sealed class OutgoingProjectExtensionImpl(
    private val project: Project,
    private val loader: String,
) : LoaderExtensionImpl(project), OutgoingProjectExtension {
    var mixinsEnabled: Boolean = false
        private set

    protected open fun UploadExtension.configureCurseforge() {}
    protected open fun UploadExtension.configureModrinth() {}

    override fun uploadToCurseforge(block: UploadExtension.() -> Unit) =
        project.uploadToCurseforge {
            modLoaders = setOf(loader)
            configureCurseforge()
            block()
        }

    override fun uploadToModrinth(block: UploadExtension.() -> Unit) =
        project.uploadToModrinth {
            modLoaders = setOf(loader)
            configureModrinth()
            block()
        }

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

internal val Project.datagenOutput
    get(): File {
        val project = if (isSubProject) project(":common") else this
        return project.file("src/generated/resources")
    }