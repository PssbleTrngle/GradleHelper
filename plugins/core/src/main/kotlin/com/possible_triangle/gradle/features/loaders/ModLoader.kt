package com.possible_triangle.gradle.features.loaders

import com.possible_triangle.gradle.DatagenBuilder
import com.possible_triangle.gradle.defaultDataGenProject
import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.stringProperty
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources
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
}

abstract class AbstractLoadExtensionWithDatagen(
    project: Project,
) : AbstractLoaderExtension(),
    DatagenBuilder,
    WithDataGen {
    abstract val project: Project

    private val _existingMods = mutableSetOf<String>()
    val existingMods: Set<String> get() = _existingMods

    final override var owner: Project? = project.defaultDataGenProject

    var enabledDataGen: Boolean = false
        private set

    var datagenSourceSet = project.objects.property<SourceSet>()

    final override fun existing(vararg mods: String) {
        this._existingMods.addAll(mods)
    }

    final override fun sourceSet(sourceSet: Provider<SourceSet>) {
        datagenSourceSet.set(sourceSet)
    }

    final override fun splitSourceSet(name: String) {
        val split =
            project.the<SourceSetContainer>().register(name) {
                compileClasspath += project.mainSourceSet.compileClasspath
                compileClasspath += project.mainSourceSet.output
                runtimeClasspath += project.mainSourceSet.runtimeClasspath
                runtimeClasspath += project.mainSourceSet.output
            }

        project.tasks.named<Jar>("sourcesJar") {
            from(split.map { it.allSource })
        }

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

val Project.mainSourceSet: SourceSet
    get() {
        val sourceSets = the<SourceSetContainer>()
        return sourceSets.getByName("main")
    }

val Project.isSubProject: Boolean get() = rootProject != project

fun Project.configureCommonProject() {
    addLoaderAttribute("common")

    val dependsOnCode =
        configurations.register("dependsOnCode") {
            isCanBeResolved = false
            isCanBeConsumed = true
        }
    val dependsOnResources =
        configurations.register("dependsOnResources") {
            isCanBeResolved = false
            isCanBeConsumed = true
        }

    artifacts {
        add(dependsOnCode.name, mainSourceSet.java.sourceDirectories.singleFile)
        // TODO kotlin
        add(dependsOnResources.name, mainSourceSet.resources.sourceDirectories.singleFile)
    }
}

val LOADER_ATTRIBUTE = Attribute.of("io.github.mcgradleconventions.loader", String::class.java)

private fun Project.addLoaderAttribute(type: String) {
    listOf("apiElements", "runtimeElements", "sourcesElements").forEach { variant ->
        configurations.named(variant) {
            attributes {
                attribute(LOADER_ATTRIBUTE, project.name)
            }
        }
    }

    the<SourceSetContainer>().configureEach {
        listOf(compileClasspathConfigurationName, runtimeClasspathConfigurationName).forEach { variant ->
            configurations.named(variant) {
                attributes {
                    attribute(LOADER_ATTRIBUTE, type)
                }
            }
        }
    }
}

fun Project.configureLoaderProject(
    config: AbstractLoaderExtension,
    loader: ModLoader,
) {
    addLoaderAttribute(loader.name.lowercase())

    val dependsOnCode = configurations.register("dependsOnCode") { isCanBeResolved = true }
    val dependsOnResources = configurations.register("dependsOnResources") { isCanBeResolved = true }

    listOf(dependsOnResources, dependsOnCode).forEach { configuration ->
        lazyDependencies(configuration.name) {
            config.dependsOn.forEach {
                add(dependencies.project(path = it.path, configuration = configuration.name))
            }
        }
    }

    lazyDependencies("compileOnly") {
        config.dependsOn.forEach {
            add(it) {
                attributes {
                    attribute(LOADER_ATTRIBUTE, "common")
                }
            }
        }
    }

    tasks.withType<ProcessResources> {
        dependsOn(dependsOnResources)
        from(dependsOnResources)
    }

    tasks.getByName<Jar>("sourcesJar") {
        dependsOn(dependsOnResources)
        from(dependsOnResources)
        dependsOn(dependsOnCode)
        from(dependsOnCode)
    }

    tasks.withType<JavaCompile> {
        dependsOn(dependsOnCode)
        source(dependsOnCode)
    }

    tasks.withType<KotlinCompile> {
        dependsOn(dependsOnCode)
        source(dependsOnCode)
    }
}

enum class ModLoader {
    FORGE,
    FABRIC,
    NEOFORGE,
}

fun ModLoader.displayName(): String =
    when (this) {
        ModLoader.NEOFORGE -> "NeoForge"
        else -> name.lowercase().capitalized()
    }

val Project.mixinExtrasVersion get() = stringProperty("mixin_extras_version") ?: "0.5.2"
