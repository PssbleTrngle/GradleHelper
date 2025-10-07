package com.possible_triangle.gradle.neoforge

import com.possible_triangle.gradle.DatagenBuilder
import com.possible_triangle.gradle.defaultDataGenProject
import com.possible_triangle.gradle.features.loaders.AbstractLoadExtensionWithDatagen
import com.possible_triangle.gradle.features.loaders.AbstractLoaderExtension
import com.possible_triangle.gradle.features.loaders.LoaderExtension
import com.possible_triangle.gradle.property
import com.possible_triangle.gradle.stringProperty
import org.gradle.api.Project
import org.gradle.api.provider.Property

interface NeoforgeExtension : LoaderExtension {
    val neoforgeVersion: Property<String>

    val kotlinForgeVersion: Property<String>

    fun dataGen(factory: DatagenBuilder.() -> Unit = {})
}

internal open class NeoforgeExtensionImpl(project: Project) : AbstractLoadExtensionWithDatagen(project),
    NeoforgeExtension {
    override val neoforgeVersion = project.objects.property(project.stringProperty("neoforge_version"))

    override var kotlinForgeVersion = project.objects.property(project.stringProperty("kotlin_forge_version"))

    var enabledDataGen: Boolean = false
        private set

    override fun dataGen(factory: DatagenBuilder.() -> Unit) {
        enabledDataGen = true
        factory(this)
    }
}