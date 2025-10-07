package com.possible_triangle.gradle.fabric

import com.possible_triangle.gradle.DatagenBuilder
import com.possible_triangle.gradle.features.loaders.AbstractLoadExtensionWithDatagen
import com.possible_triangle.gradle.features.loaders.LoaderExtension
import com.possible_triangle.gradle.property
import com.possible_triangle.gradle.stringProperty
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.provider.Property

interface FabricExtension : LoaderExtension {
    val apiVersion: Property<String>
    val loaderVersion: Property<String>

    val kotlinFabricVersion: Property<String>

    fun dataGen(factory: DatagenBuilder.() -> Unit = {})

    fun mappings(supplier: LoomGradleExtensionAPI.() -> Dependency)
}

internal open class FabricExtensionImpl(project: Project) : AbstractLoadExtensionWithDatagen(project), FabricExtension {
    override val loaderVersion = project.objects.property(project.stringProperty("fabric_loader_version"))
    override val apiVersion = project.objects.property(project.stringProperty("fabric_api_version"))
    override val kotlinFabricVersion = project.objects.property(project.stringProperty("kotlin_fabric_version"))

    var enabledDataGen: Boolean = false
        private set

    var mappingsSupplier: LoomGradleExtensionAPI.() -> Dependency = { officialMojangMappings() }
        private set

    override fun dataGen(factory: DatagenBuilder.() -> Unit) {
        enabledDataGen = true
        factory(this)
    }

    override fun mappings(supplier: LoomGradleExtensionAPI.() -> Dependency) {
        this.mappingsSupplier = supplier
    }
}