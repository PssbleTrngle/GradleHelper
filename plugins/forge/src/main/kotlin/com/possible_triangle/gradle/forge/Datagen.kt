package com.possible_triangle.gradle.forge

import com.possible_triangle.gradle.mod
import net.neoforged.moddevgradle.legacyforge.dsl.LegacyForgeExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the

internal fun Project.configureDatagenRun() {
    val config = the<ForgeExtension>() as ForgeExtensionImpl

    configure<LegacyForgeExtension> {
        if (config.enabledDataGen) {
            runs.named("data") {
                val existingResources = config.existingResources.flatMap { listOf("--existing", it.path) }
                val existingMods = config.existingMods.flatMap { listOf("--existing-mod", it) }
                val dataGenArgs =
                    listOf(
                        "--mod",
                        mod.id.get(),
                        "--all",
                        "--output",
                        config.datagenOutput.path,
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
