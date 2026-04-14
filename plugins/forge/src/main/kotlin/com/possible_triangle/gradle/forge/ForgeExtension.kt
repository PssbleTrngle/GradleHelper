package com.possible_triangle.gradle.forge

import com.possible_triangle.gradle.access.generateAccessTransformer
import com.possible_triangle.gradle.features.loaders.*
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.property
import com.possible_triangle.gradle.stringProperty
import net.neoforged.moddevgradle.legacyforge.dsl.LegacyForgeExtension
import net.neoforged.nfrtgradle.CreateMinecraftArtifacts
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import java.io.File

interface ForgeExtension : LoaderExtension, WithAccessWidener, WithAccessTransformer, WithDataGen {
    val forgeVersion: Provider<String>

    val kotlinForgeVersion: Property<String>

    fun enableMixins()
}

internal open class ForgeExtensionImpl(override val project: Project) : AbstractLoadExtensionWithDatagen(project),
    ForgeExtension {
    override val forgeVersion = project.provider { project.stringProperty("forge_version") }

    override val kotlinForgeVersion = project.objects.property(project.stringProperty("kotlin_forge_version"))

    var mixinsEnabled: Boolean = false
        private set

    override fun enableMixins() {
        mixinsEnabled = true
    }

    override fun accessTransformer(file: Provider<File>) {
        project.the<LegacyForgeExtension>().setAccessTransformers(file)
    }

    override fun accessWidener(file: Provider<File>) {
        val (output, task) = project.generateAccessTransformer(file)
        project.tasks.withType<CreateMinecraftArtifacts> {
            dependsOn(task)
        }
        accessTransformer(output)
    }

}