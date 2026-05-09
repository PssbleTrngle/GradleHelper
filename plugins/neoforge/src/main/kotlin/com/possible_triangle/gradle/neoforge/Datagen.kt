package com.possible_triangle.gradle.neoforge

import com.possible_triangle.gradle.configureDatagen
import com.possible_triangle.gradle.datagenOutput
import com.possible_triangle.gradle.existingResources
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.requireOwner
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import net.neoforged.moddevgradle.internal.utils.VersionCapabilitiesInternal
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import kotlin.collections.plus

internal fun Project.hasSplitDataRuns(): Boolean {
    val version =
        mod.minecraftVersion
            .map {
                VersionCapabilitiesInternal.ofMinecraftVersion(it)
            }.getOrElse(
                VersionCapabilitiesInternal.latest(),
            )
    return version.splitDataRuns()
}

internal fun Project.configureDatagenRun() {
    val config = the<NeoforgeExtension>() as NeoforgeExtensionImpl

    configure<NeoForgeExtension> {
        if (config.enabledDataGen) {
            config.requireOwner().configureDatagen()

            runs.named("data") {
                gameDirectory = project.file("run/data")

                val existingResources = existingResources.flatMap { listOf("--existing", it.path) }
                val existingMods = config.existingMods.flatMap { listOf("--existing-mod", it) }
                val dataGenArgs =
                    listOf(
                        "--mod",
                        mod.id.get(),
                        "--all",
                        "--output",
                        config.requireOwner().datagenOutput.path,
                    ) + existingResources + existingMods

                programArguments.addAll(dataGenArgs)

                config.datagenSourceSet.orNull?.let {
                    sourceSet.set(it)
                }
            }
        } else {
            runs.removeIf { it.name == "data" }
        }
    }
}
