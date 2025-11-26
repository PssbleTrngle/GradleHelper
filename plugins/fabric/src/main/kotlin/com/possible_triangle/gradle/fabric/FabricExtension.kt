package com.possible_triangle.gradle.fabric

import com.possible_triangle.gradle.features.loaders.AbstractLoadExtensionWithDatagen
import com.possible_triangle.gradle.features.loaders.LoaderExtension
import com.possible_triangle.gradle.features.loaders.WithAccessWidener
import com.possible_triangle.gradle.features.loaders.WithDataGen
import com.possible_triangle.gradle.features.loaders.WithInterfaceInjections
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.property
import com.possible_triangle.gradle.stringProperty
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.filter
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import java.io.File

interface FabricExtension : LoaderExtension, WithAccessWidener, WithDataGen, WithInterfaceInjections {
    val apiVersion: Property<String>
    val loaderVersion: Property<String>

    val kotlinFabricVersion: Property<String>

    fun mappings(supplier: LoomGradleExtensionAPI.() -> Dependency)
}

internal open class FabricExtensionImpl(override val project: Project) : AbstractLoadExtensionWithDatagen(project),
    FabricExtension {
    override val loaderVersion = project.objects.property(project.stringProperty("fabric_loader_version"))
    override val apiVersion = project.objects.property(project.stringProperty("fabric_api_version"))
    override val kotlinFabricVersion = project.objects.property(project.stringProperty("kotlin_fabric_version"))

    var mappingsSupplier: LoomGradleExtensionAPI.() -> Dependency = { officialMojangMappings() }
        private set

    override fun mappings(supplier: LoomGradleExtensionAPI.() -> Dependency) {
        this.mappingsSupplier = supplier
    }

    override fun accessWidener(file: Provider<File>) {
        project.the<LoomGradleExtensionAPI>().accessWidenerPath.set { file.get() }
    }

    override fun injectInterfaces(file: Provider<File>) {
        project.tasks.withType<Jar> {
            filesMatching("fabric.mod.json") {
                filter(AddInterfaceInjections::class, "from" to file.get())
            }
        }
    }

}