package com.possible_triangle.gradle.fabric

import com.possible_triangle.gradle.configureDatagen
import com.possible_triangle.gradle.datagenOutput
import com.possible_triangle.gradle.existingResources
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.requireOwner
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

internal fun Project.configureDatagenRun() {
    val config = the<FabricExtension>() as FabricExtensionImpl

    if (config.enabledDataGen) {
        config.requireOwner().configureDatagen()

        loom.runs {
            named("data") {
                client()
                configName = "Fabric Datagen"
                runDir("run/data")

                property("fabric-api.datagen")
                property("fabric-api.datagen.output-dir", "${config.requireOwner().datagenOutput}")
                property("fabric-api.datagen.modid", mod.id.get())
                property("porting_lib.datagen.existing_resources", "${existingResources.first()}")
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
