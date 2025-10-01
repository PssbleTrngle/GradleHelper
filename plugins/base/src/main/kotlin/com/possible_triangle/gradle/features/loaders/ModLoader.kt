package com.possible_triangle.gradle.features.loaders

import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.stringProperty
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.minecraftforge.gradle.userdev.DependencyManagementExtension
import net.neoforged.gradle.userdev.runtime.extension.UserDevRuntimeExtension
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

interface LoaderExtension {
    fun dependOn(vararg projects: Project)
    val libraries: Included
}

internal sealed class LoaderExtensionImpl(private val project: Project) : LoaderExtension {
    private val _dependsOn = arrayListOf<Project>()
    override val libraries = IncludedImpl(project, project.mod.libraries)

    val dependsOn get() = _dependsOn.toSet()

    override fun dependOn(vararg projects: Project) {
        _dependsOn.addAll(projects)
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
    val mods: Included
}

internal sealed class OutgoingProjectExtensionImpl(private val project: Project) : LoaderExtensionImpl(project),
    OutgoingProjectExtension {
    var mixinsEnabled: Boolean = false
        private set

    override val mods = IncludedImpl(project, project.mod.mods)

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

internal val Project.mixinExtrasVersion get() = stringProperty("mixin_extras_version")