package com.possible_triangle.gradle

import com.possible_triangle.gradle.features.loaders.Included
import com.possible_triangle.gradle.features.loaders.IncludedImpl
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.hasPlugin
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.the

val Project.coreProject get() = rootProject.takeIf { it.plugins.hasPlugin(GradleHelperCorePlugin::class) } ?: this

fun ExtensionAware.mod(block: ModExtension.() -> Unit) = extensions.configure(block)
val Project.mod get() = the<ModExtension>()

interface ModExtension {
    val id: Property<String>
    val name: Property<String>
    val version: Property<String>
    val author: Property<String>
    val minecraftVersion: Property<String>
    val releaseType: Property<String>
    val repository: Property<String>
    val mavenGroup: Property<String>

    val libraries: Included
    val mods: Included
}

internal open class ModExtensionImpl(project: Project) : ModExtension {
    override val id: Property<String> = project.objects.property()
    override val name: Property<String> = project.objects.property()
    override val version: Property<String> = project.objects.property()
    override val author: Property<String> = project.objects.property()
    override val minecraftVersion: Property<String> = project.objects.property()
    override val releaseType: Property<String> = project.objects.property()
    override val repository: Property<String> = project.objects.property()
    override val mavenGroup: Property<String> = project.objects.property()
    override val libraries: Included = IncludedImpl(project, project.parent?.mod?.libraries)
    override val mods: Included = IncludedImpl(project, project.parent?.mod?.mods)
}