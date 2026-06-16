package com.possible_triangle.gradle.neoforge

import com.possible_triangle.gradle.mod
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the

internal fun Project.configureDatagenRun() {
    val config = the<NeoforgeExtension>() as NeoforgeExtensionImpl

    configure<NeoForgeExtension> {
        if (config.enabledDataGen) {
            runs.named("data") {
                gameDirectory = project.file("run/data")

                val existingResources = listOf("--existing", config.existingResources.path)
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
