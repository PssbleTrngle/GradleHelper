package com.possible_triangle.gradle.fabric

import com.possible_triangle.gradle.mod
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

internal fun Project.configureDatagenRun() {
    val config = the<FabricExtension>() as FabricExtensionImpl

    if (config.enabledDataGen) {
        loom.runs {
            named("data") {
                client()
                configName = "Fabric Datagen"
                runDir("run/data")

                property("fabric-api.datagen")
                property("fabric-api.datagen.output-dir", "${config.datagenOutput}")
                property("fabric-api.datagen.modid", mod.id.get())
                property("porting_lib.datagen.existing_resources", "${config.existingResources}")
                if (config.existingMods.isNotEmpty()) {
                    property("porting_lib.datagen.existing-mod", config.existingMods.joinToString(","))
                }

                config.datagenSourceSet.orNull?.let {
                    source(it)
                }
            }
        }
    } else {
        loom.runs.removeIf { it.name == "data" }
    }
}
