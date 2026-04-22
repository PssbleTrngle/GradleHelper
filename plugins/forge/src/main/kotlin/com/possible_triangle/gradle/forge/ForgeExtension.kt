package com.possible_triangle.gradle.forge

import com.possible_triangle.gradle.access.generateAccessTransformer
import com.possible_triangle.gradle.features.loaders.*
import com.possible_triangle.gradle.property
import com.possible_triangle.gradle.stringProperty
import net.neoforged.moddevgradle.legacyforge.dsl.LegacyForgeExtension
import net.neoforged.nfrtgradle.CreateMinecraftArtifacts
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import java.io.File

interface ForgeExtension : LoaderExtension, WithAccessWidener, WithAccessTransformer, WithDataGen,
    WithInterfaceInjections {
    val forgeVersion: Provider<String>

    val kotlinForgeVersion: Property<String>

    val parchmentMappingsVersion: Property<String>

    fun enableMixins()
}

internal open class ForgeExtensionImpl(override val project: Project) : AbstractLoadExtensionWithDatagen(project),
    ForgeExtension {
    override val forgeVersion = project.provider { project.stringProperty("forge_version") }

    override val kotlinForgeVersion = project.objects.property(project.stringProperty("kotlin_forge_version"))

    override val parchmentMappingsVersion = project.objects.property(project.stringProperty("parchment_mappings_version"))

    var mixinsEnabled: Boolean = false
        private set

    override fun enableMixins() {
        mixinsEnabled = true
    }

    override fun accessTransformer(file: Provider<File>) {
        project.configure<LegacyForgeExtension> {
            accessTransformers {
                from(file)
                publish(file)
            }
        }
    }

    override fun accessWidener(file: Provider<File>) {
        val (output, task) = project.generateAccessTransformer(file)
        accessTransformer(output)

        project.tasks.withType<CreateMinecraftArtifacts> {
            dependsOn(task)
        }
        project.tasks.named("copyAccessTransformersPublications") {
            dependsOn(task)
        }
    }

    override fun injectInterfaces(file: Provider<File>) {
        project.configure<LegacyForgeExtension> {
            interfaceInjectionData {
                from(file)
                publish(file)
            }
        }
    }

}