package com.possible_triangle.gradle.neoforge

import com.possible_triangle.gradle.mod
import net.neoforged.moddevgradle.dsl.ModModel
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

                config.datagenSourceSet.orNull?.let { dataSourceSet ->
                    // mod source sets are used to decide which classes are loaded
                    // in order to actually only load the data source set during data runs and not during client runs,
                    // we have to give this data run a "fake" copy of the mod
                    loadedMods =
                        loadedMods.get().map {
                            if (it.name == mod.id.get()) {
                                val cloned = objects.newInstance(ModModel::class.java, it.name)
                                cloned.modSourceSets =
                                    it.modSourceSets.map { sets ->
                                        sets + dataSourceSet
                                    }
                                cloned
                            } else {
                                it
                            }
                        }

                    sourceSet.set(dataSourceSet)
                }
            }
        } else {
            runs.removeIf { it.name == "data" }
        }
    }
}
