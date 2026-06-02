package com.possible_triangle.gradle

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.hasPlugin
import org.gradle.kotlin.dsl.listProperty
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
    val description: Property<String>
    val minecraftVersion: Property<String>
    val releaseType: Property<String>
    val repository: Property<String>
    val mavenGroup: Property<String>

    val additional: AdditionalProperties
}

interface AdditionalProperties {
    fun add(
        key: String,
        value: String,
    )

    fun add(
        key: String,
        value: Provider<String>,
    )

    fun add(key: String)

    fun toMap(): Map<String, Provider<String>>

    val files: ListProperty<String>
}

internal open class ModExtensionImpl(
    project: Project,
) : ModExtension {
    override val id: Property<String> = project.objects.property()
    override val name: Property<String> = project.objects.property()
    override val version: Property<String> = project.objects.property()
    override val author: Property<String> = project.objects.property()
    override val description: Property<String> = project.objects.property()
    override val minecraftVersion: Property<String> = project.objects.property()
    override val releaseType: Property<String> = project.objects.property()
    override val repository: Property<String> = project.objects.property()
    override val mavenGroup: Property<String> = project.objects.property()
    override val additional: AdditionalPropertiesImpl =
        AdditionalPropertiesImpl(project, project.parent?.mod?.additional)
}

internal class AdditionalPropertiesImpl(
    private val project: Project,
    private val parent: AdditionalProperties?,
) : AdditionalProperties {
    private val values = mutableMapOf<String, Provider<String>>()

    override fun add(
        key: String,
        value: String,
    ) = add(key, project.provider { value })

    override fun add(
        key: String,
        value: Provider<String>,
    ) {
        values[key] = value
    }

    override fun add(key: String) = add(key, project.providers.gradleProperty(key))

    override fun toMap() = (parent?.toMap() ?: emptyMap()) + values.toMap()

    override val files =
        project.objects.listProperty<String>().convention(
            project.provider {
                listOfNotNull(
                    "META-INF/mods.toml",
                    "META-INF/neoforge.mods.toml",
                    "pack.mcmeta",
                    "fabric.mod.json",
                    project.mod.id
                        .map { modId -> "$modId*.mixins.json" }
                        .orNull,
                )
            },
        )
}

internal fun ModExtension.resolveProperties(): Map<String, String> {
    val mcVersionRange = minecraftVersion.map { "[$it,)" }

    val providers =
        mapOf(
            "version" to version,
            "mod_version" to version,
            "mod_name" to name,
            "mod_id" to id,
            "mod_author" to author,
            "mod_description" to description,
            "repository" to repository,
            "minecraft_version" to minecraftVersion,
            "mc_version" to minecraftVersion,
            "minecraft_version_range" to mcVersionRange,
            "mc_version_range" to mcVersionRange,
        ) + additional.toMap()

    return providers
        .filterValues { it.isPresent }
        .mapValues { it.value.get() }
}
