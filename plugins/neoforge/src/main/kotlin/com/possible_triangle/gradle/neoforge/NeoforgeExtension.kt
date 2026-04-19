package com.possible_triangle.gradle.neoforge

import com.possible_triangle.gradle.access.generateAccessTransformer
import com.possible_triangle.gradle.features.loaders.AbstractLoadExtensionWithDatagen
import com.possible_triangle.gradle.features.loaders.LoaderExtension
import com.possible_triangle.gradle.features.loaders.WithAccessTransformer
import com.possible_triangle.gradle.features.loaders.WithAccessWidener
import com.possible_triangle.gradle.features.loaders.WithDataGen
import com.possible_triangle.gradle.features.loaders.WithInterfaceInjections
import com.possible_triangle.gradle.property
import com.possible_triangle.gradle.stringProperty
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import net.neoforged.nfrtgradle.CreateMinecraftArtifacts
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import java.io.File

interface NeoforgeExtension : LoaderExtension, WithAccessWidener, WithAccessTransformer, WithDataGen,
    WithInterfaceInjections {
    val neoforgeVersion: Property<String>
    val kotlinForgeVersion: Property<String>
    val parchmentMappingsVersion: Property<String>
}

internal open class NeoforgeExtensionImpl(override val project: Project) : AbstractLoadExtensionWithDatagen(project),
    NeoforgeExtension {
    override val neoforgeVersion = project.objects.property(project.stringProperty("neoforge_version"))

    override val kotlinForgeVersion = project.objects.property(project.stringProperty("kotlin_forge_version"))

    override val parchmentMappingsVersion = project.objects.property(project.stringProperty("parchment_mappings_version"))

    override fun accessTransformer(file: Provider<File>) {
        project.configure<NeoForgeExtension> {
            accessTransformers {
                from(file)
                publish(file)
            }
        }
    }

    override fun accessWidener(file: Provider<File>) {
        val (output, task) = project.generateAccessTransformer(file)
        project.tasks.withType<CreateMinecraftArtifacts> {
            dependsOn(task)
        }
        accessTransformer(output)
    }

    override fun injectInterfaces(file: Provider<File>) {
        project.configure<NeoForgeExtension> {
            interfaceInjectionData {
                from(file)
                publish(file)
            }
        }
    }

}