package com.possible_triangle.gradle.common

import com.possible_triangle.gradle.access.generateAccessTransformer
import com.possible_triangle.gradle.features.loaders.*
import com.possible_triangle.gradle.mod
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

interface CommonExtension :
    LoaderExtension,
    WithAccessWidener,
    WithAccessTransformer,
    WithInterfaceInjections {
    val neoformVersion: Property<String>
    val parchmentMappingsVersion: Property<String>
}

internal open class CommonExtensionImpl(
    override val project: Project,
) : AbstractLoaderExtension(),
    CommonExtension {
    override val neoformVersion =
        project.objects.property(
            project.mod.minecraftVersion.map {
                NeoformFetcher.fetchFor(it)
            },
        )

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
