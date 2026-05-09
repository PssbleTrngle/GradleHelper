package com.possible_triangle.gradle.forge

import com.possible_triangle.gradle.configureDatagen
import com.possible_triangle.gradle.datagenOutput
import com.possible_triangle.gradle.existingResources
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.requireOwner
import net.neoforged.moddevgradle.legacyforge.dsl.LegacyForgeExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import kotlin.collections.plus

internal fun Project.configureDatagenRun() {
    val config = the<ForgeExtension>() as ForgeExtensionImpl

    configure<LegacyForgeExtension> {
        if (config.enabledDataGen) {
            config.requireOwner().configureDatagen()

            runs.named("data") {
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
