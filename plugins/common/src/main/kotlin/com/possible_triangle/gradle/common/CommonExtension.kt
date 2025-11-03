package com.possible_triangle.gradle.common

import com.possible_triangle.gradle.access.generateAccessTransformer
import com.possible_triangle.gradle.features.loaders.AbstractLoaderExtension
import com.possible_triangle.gradle.features.loaders.LoaderExtension
import com.possible_triangle.gradle.features.loaders.WithAccessTransformer
import com.possible_triangle.gradle.features.loaders.WithAccessWidener
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.property
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.the
import java.io.File

interface CommonExtension : LoaderExtension, WithAccessWidener, WithAccessTransformer {
    val neoformVersion: Property<String>
}

internal open class CommonExtensionImpl(override val project: Project) : AbstractLoaderExtension(), CommonExtension {
    override val neoformVersion = project.objects.property(project.mod.minecraftVersion.map {
        NeoformFetcher.fetchFor(it)
    })

    override fun accessTransformer(file: Provider<File>) {
        project.the<NeoForgeExtension>().setAccessTransformers(file)
    }

    override fun accessWidener(file: Provider<File>) {
        val output = project.generateAccessTransformer(file)
        accessTransformer(output)
    }

}