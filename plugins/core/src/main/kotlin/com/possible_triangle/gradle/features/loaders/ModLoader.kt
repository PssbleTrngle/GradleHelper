package com.possible_triangle.gradle.features.loaders

import com.possible_triangle.gradle.DatagenBuilder
import com.possible_triangle.gradle.defaultDataGenProject
import com.possible_triangle.gradle.stringProperty
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

interface LoaderExtension {
    fun dependOn(vararg projects: Project)
}

abstract class AbstractLoaderExtension() : LoaderExtension {
    private val _dependsOn = arrayListOf<Project>()

    val dependsOn get() = _dependsOn.toSet()

    override fun dependOn(vararg projects: Project) {
        _dependsOn.addAll(projects)
    }
}

interface WithDataGen {
    fun dataGen(factory: DatagenBuilder.() -> Unit = {})
}

abstract class AbstractLoadExtensionWithDatagen(project: Project) : AbstractLoaderExtension(), DatagenBuilder,
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
        val split = project.the<SourceSetContainer>().register(name) {
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
}

val Project.mainSourceSet: SourceSet
    get() {
        val sourceSets = the<SourceSetContainer>()
        return sourceSets.getByName("main")
    }

val Project.isSubProject: Boolean get() = rootProject != project

fun Project.configureOutputProject(config: AbstractLoaderExtension) {
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

enum class ModLoader {
    FORGE, FABRIC, NEOFORGE
}

// internal fun Project.detectModLoader(): ModLoader? {
//     return extensions.findByType<DependencyManagementExtension>()?.let {
//         ModLoader.FORGE
//     } ?: extensions.findByType<LoomGradleExtensionAPI>()?.let {
//         ModLoader.FABRIC
//     } ?: extensions.findByType<UserDevRuntimeExtension>()?.let {
//         ModLoader.NEOFORGE
//     }
// }

val Project.mixinExtrasVersion get() = stringProperty("mixin_extras_version") ?: "0.4.1"