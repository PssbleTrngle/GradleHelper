package com.possible_triangle.gradle.neoforge

import com.possible_triangle.gradle.access.generateAccessTransformer
import com.possible_triangle.gradle.features.loaders.AbstractLoadExtensionWithDatagen
import com.possible_triangle.gradle.features.loaders.LoaderExtension
import com.possible_triangle.gradle.features.loaders.WithAccessTransformer
import com.possible_triangle.gradle.features.loaders.WithAccessWidener
import com.possible_triangle.gradle.features.loaders.WithDataGen
import com.possible_triangle.gradle.property
import com.possible_triangle.gradle.stringProperty
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.the
import java.io.File

interface NeoforgeExtension : LoaderExtension, WithAccessWidener, WithAccessTransformer, WithDataGen {
    val neoforgeVersion: Property<String>

    val kotlinForgeVersion: Property<String>
}

internal open class NeoforgeExtensionImpl(override val project: Project) : AbstractLoadExtensionWithDatagen(project),
    NeoforgeExtension {
    override val neoforgeVersion = project.objects.property(project.stringProperty("neoforge_version"))

    override var kotlinForgeVersion = project.objects.property(project.stringProperty("kotlin_forge_version"))

    override fun accessTransformer(file: Provider<File>) {
        project.the<NeoForgeExtension>().setAccessTransformers(file)
    }

    override fun accessWidener(file: Provider<File>) {
        val output = project.generateAccessTransformer(file)
        accessTransformer(output)
    }

}