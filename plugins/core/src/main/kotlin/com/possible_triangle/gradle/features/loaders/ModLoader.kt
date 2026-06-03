package com.possible_triangle.gradle.features.loaders

import com.possible_triangle.gradle.DatagenBuilder
import com.possible_triangle.gradle.configureDatagen
import com.possible_triangle.gradle.datagenOutput
import com.possible_triangle.gradle.defaultDataGenProject
import com.possible_triangle.gradle.existingResources
import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.property
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.AttributeCompatibilityRule
import org.gradle.api.attributes.CompatibilityCheckDetails
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

    final override val owner = project.objects.property(project.provider { project.defaultDataGenProject })

    val datagenOutput get() = owner.get().datagenOutput
    val existingResources get() = owner.get().existingResources

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

    configureDatagen(datagenOutput, dependsOnResources.name)

    artifacts {
        mainSourceSet.java.sourceDirectories.files
            .forEach { add(dependsOnCode.name, it) }
        mainSourceSet.resources.sourceDirectories.files
            .forEach { add(dependsOnResources.name, it) }
    }
}

val LOADER_ATTRIBUTE = Attribute.of("io.github.mcgradleconventions.loader", String::class.java)

private fun Project.addLoaderAttribute(type: String) {
    addLoaderCompatibilityRule()

    listOf("apiElements", "runtimeElements", "sourcesElements").forEach { variant ->
        configurations.named(variant) {
            attributes {
                attribute(LOADER_ATTRIBUTE, type)
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

private class LoaderCompatibilityRule : AttributeCompatibilityRule<String> {
    override fun execute(details: CompatibilityCheckDetails<String>) {
        if (details.producerValue == "common") {
            details.compatible()
        }
    }
}

private fun Project.addLoaderCompatibilityRule() {
    dependencies {
        attributesSchema {
            attribute(LOADER_ATTRIBUTE) {
                compatibilityRules.add(LoaderCompatibilityRule::class.java)
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

    configureDatagen(datagenOutput, dependsOnResources.name)

    listOf(dependsOnResources, dependsOnCode).forEach { configuration ->
        lazyDependencies(configuration.name) {
            config.dependsOn.forEach {
                add(dependencies.project(path = it.path, configuration = configuration.name))
            }
        }
    }

    lazyDependencies("compileOnly") {
        config.dependsOn.forEach {
            add(it)
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

val Project.mixinExtrasVersion get() = providers.gradleProperty("mixin_extras_version").getOrElse("0.5.2")
