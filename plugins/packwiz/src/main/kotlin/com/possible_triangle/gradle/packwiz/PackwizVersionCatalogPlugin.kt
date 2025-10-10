package com.possible_triangle.gradle.packwiz

import org.apache.log4j.LogManager
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.create

@Suppress("unused")
class PackwizVersionCatalogPlugin : Plugin<Settings> {

    private val logger = LogManager.getLogger(PackwizVersionCatalogPlugin::class.java)

    override fun apply(target: Settings) {
        val extension = target.extensions.create<PackwizExtension>("packwiz")

        extension.strategy.convention(ErrorStrategy.FAIL)
        extension.verbose.convention(false)

        extension.packs.create(DEFAULT_PACK_NAME) {
            strategy.set(ErrorStrategy.SKIP)
            from.set { target.rootDir.resolve("pack") }
        }

        val importer = PackwizVersionCatalog(extension, logger)
        importer.importPackwiz(target)
    }

}