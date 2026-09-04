package com.possible_triangle.gradle.features.loaders

import com.possible_triangle.gradle.*
import com.possible_triangle.gradle.features.lazyDependencies
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

interface LoaderExtension {
    fun dependOn(vararg projects: Project)
}

abstract class AbstractLoaderExtension : LoaderExtension {
    private val _dependsOn = arrayListOf<Project>()

    val dependsOn get() = _dependsOn.toSet()

    override fun dependOn(vararg projects: Project) {
        _dependsOn.addAll(projects)
    }
}

interface WithDataGen {
    fun dataGen(factory: DatagenBuilder.() -> Unit = {})

    fun modSourceSets(): List<SourceSet>

    fun createDataSourceSet(name: String = "data"): NamedDomainObjectProvider<SourceSet>
}

abstract class AbstractLoadExtensionWithDatagen(
    project: Project,
) : AbstractLoaderExtension(),
    DatagenBuilder,
    WithDataGen {
    abstract val project: Project

    private val _existingMods = mutableSetOf<String>()
    val existingMods: Set<String> get() = _existingMods

    final override val owner = project.objects.property(project.provider { project.defaultDataGenProject })

    val datagenOutput get() = owner.get().datagenOutput
    val existingResources get() = owner.get().existingResources

    var enabledDataGen: Boolean = false
        private set

    val datagenSourceSet = project.objects.property<SourceSet>()

    final override fun existing(vararg mods: String) {
        this._existingMods.addAll(mods)
    }

    final override fun sourceSet(sourceSet: Provider<SourceSet>) {
        datagenSourceSet.set(sourceSet)
    }

    override fun createDataSourceSet(name: String): NamedDomainObjectProvider<SourceSet> {
        val sourceSet =
            project.the<SourceSetContainer>().register(name) {
                compileClasspath += project.mainSourceSet.compileClasspath
                compileClasspath += project.mainSourceSet.output
                runtimeClasspath += project.mainSourceSet.runtimeClasspath
                runtimeClasspath += project.mainSourceSet.output
            }

        val dataCompileClassPath = project.configurations.getByName("${name}CompileClasspath")
        val dataRuntimeClasspath = project.configurations.getByName("${name}RuntimeClasspath")

        sourceSet.configure {
            compileClasspath += dataCompileClassPath
            runtimeClasspath += dataRuntimeClasspath

            java.sourceDirectories.files.forEach {
                project.artifacts.add("dataElements", it)
            }
        }

        val compilePrefix = "compile${name.capitalized()}"

        project.tasks.named<JavaCompile>("${compilePrefix}Java") {
            dependsOn(dataCompileClassPath)
            source(dataCompileClassPath)
        }

        project.tasks.withType<KotlinCompile> {
            if (name != "${compilePrefix}Kotlin") return@withType
            dependsOn(dataCompileClassPath)
            source(dataCompileClassPath)
        }

        project.lazyDependencies("${name}Implementation") {
            dependsOn.forEach {
                add(project.dependencies.project(path = it.path, configuration = "dataElements"))
            }
        }

        return sourceSet
    }

    final override fun splitSourceSet(name: String) {
        val split = createDataSourceSet(name)
        sourceSet(split)
    }

    final override fun dataGen(factory: DatagenBuilder.() -> Unit) {
        enabledDataGen = true
        factory(this)
    }

    override fun modSourceSets(): List<SourceSet> {
        // val dependencies = dependsOn.map { it.mainSourceSet }
        val datagen = listOfNotNull(datagenSourceSet.orNull)
        // return dependencies + datagen
        return datagen
    }
}
