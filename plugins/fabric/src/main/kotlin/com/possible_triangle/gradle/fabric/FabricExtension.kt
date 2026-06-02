package com.possible_triangle.gradle.fabric

import com.possible_triangle.gradle.features.loaders.AbstractLoadExtensionWithDatagen
import com.possible_triangle.gradle.features.loaders.LoaderExtension
import com.possible_triangle.gradle.features.loaders.WithAccessWidener
import com.possible_triangle.gradle.features.loaders.WithDataGen
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.property
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.the
import java.io.File

interface FabricExtension :
    LoaderExtension,
    WithAccessWidener,
    WithDataGen {
    val apiVersion: Property<String>
    val loaderVersion: Property<String>

    val kotlinFabricVersion: Property<String>

    val parchmentMappingsVersion: Property<String>
}

internal open class FabricExtensionImpl(
    override val project: Project,
) : AbstractLoadExtensionWithDatagen(project),
    FabricExtension {
    override val loaderVersion = project.objects.property(project.providers.gradleProperty("fabric_loader_version"))
    override val apiVersion = project.objects.property(project.providers.gradleProperty("fabric_api_version"))

    override val kotlinFabricVersion = project.objects.property(project.providers.gradleProperty("kotlin_fabric_version"))

    override val parchmentMappingsVersion =
        project.objects.property(project.providers.gradleProperty("parchment_mappings_version"))

    override fun accessWidener(file: Provider<File>) {
        project.the<LoomGradleExtensionAPI>().accessWidenerPath.set { file.get() }
    }

    /*
    TODO Needs a transformer into an access-widener/class-tweaker
    override fun injectInterfaces(file: Provider<File>) {
        project.tasks.withType<Jar> {
            filesMatching("fabric.mod.json") {
                filter(AddInterfaceInjections::class, "from" to file.get())
            }
        }
    }
     */
}
