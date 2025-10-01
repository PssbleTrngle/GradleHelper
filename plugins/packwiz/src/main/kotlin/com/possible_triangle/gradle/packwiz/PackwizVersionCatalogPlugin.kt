package com.possible_triangle.gradle.packwiz

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logging
import org.gradle.kotlin.dsl.create

internal val LOGGER = Logging.getLogger(PackwizVersionCatalogPlugin::class.java)

@Suppress("unused")
class PackwizVersionCatalogPlugin : Plugin<Settings> {

    override fun apply(target: Settings) {
        val extension = target.extensions.create<PackwizExtension>("packwiz")

        extension.strategy.convention(ErrorStrategy.FAIL)
        extension.modrinth.convention(true)
        extension.curseforge.convention(true)
        extension.from.convention { target.rootDir.resolve("pack") }
        extension.name.convention("pack")

        target.dependencyResolutionManagement {
            importPackwiz(extension)
        }
    }

}