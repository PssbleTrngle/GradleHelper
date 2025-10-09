package com.possible_triangle.gradle.common

import com.possible_triangle.gradle.features.loaders.AbstractLoaderExtension
import com.possible_triangle.gradle.features.loaders.LoaderExtension
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.property
import org.gradle.api.Project
import org.gradle.api.provider.Property

interface CommonExtension : LoaderExtension {
    val neoformVersion: Property<String>
}

internal open class CommonExtensionImpl(project: Project) : AbstractLoaderExtension(), CommonExtension {
    override val neoformVersion = project.objects.property(project.mod.minecraftVersion.map {
        NeoformFetcher.fetchFor(it)
    })
}