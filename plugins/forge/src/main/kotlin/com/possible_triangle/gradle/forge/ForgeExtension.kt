package com.possible_triangle.gradle.forge

import com.possible_triangle.gradle.DatagenBuilder
import com.possible_triangle.gradle.features.loaders.AbstractLoadExtensionWithDatagen
import com.possible_triangle.gradle.features.loaders.LoaderExtension
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.property
import com.possible_triangle.gradle.stringProperty
import org.gradle.api.Project
import org.gradle.api.provider.Property

interface ForgeExtension : LoaderExtension {
    val mappingChannel: Property<String>
    val mappingVersion: Property<String>
    val forgeVersion: Property<String>

    val kotlinForgeVersion: Property<String>

    fun dataGen(factory: DatagenBuilder.() -> Unit = {})
    fun enableMixins(): Unit
}

internal open class ForgeExtensionImpl(private val project: Project) : AbstractLoadExtensionWithDatagen(project),
    ForgeExtension {
    override var mappingChannel = project.objects.property("official")
    override var mappingVersion = project.objects.property(project.mod.minecraftVersion)
    override var forgeVersion = project.objects.property(project.stringProperty("forge_version"))

    override var kotlinForgeVersion = project.objects.property(project.stringProperty("kotlin_forge_version"))

    var enabledDataGen: Boolean = false
        private set

    override fun dataGen(factory: DatagenBuilder.() -> Unit) {
        enabledDataGen = true
        factory(this)
    }

    var mixinsEnabled: Boolean = false
        private set

    override fun enableMixins() {
        mixinsEnabled = true
        project.enableMixins()
    }
}