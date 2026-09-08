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
                displayName.set("Fabric Datagen")
                runDirectory.set(file("run/data"))

                systemProperties.put("fabric-api.datagen", "")
                systemProperties.put("fabric-api.datagen.output-dir", "${config.datagenOutput}")
                systemProperties.put("fabric-api.datagen.modid", mod.id.get())
                systemProperties.put("porting_lib.datagen.existing_resources", "${config.existingResources.first()}")
                if (config.existingMods.isNotEmpty()) {
                    systemProperties.put("porting_lib.datagen.existing-mod", config.existingMods.joinToString(","))
                }

                config.datagenSourceSet.orNull?.let {
                    sourceSet.set(it.name)
                }
            }
        }
    } else {
        loom.runs.removeIf { it.name == "data" }
    }
}
