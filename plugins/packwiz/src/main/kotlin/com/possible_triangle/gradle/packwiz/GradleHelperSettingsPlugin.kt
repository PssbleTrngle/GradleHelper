package com.possible_triangle.gradle.packwiz

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class PackwizVersionCatalogPlugin : Plugin<Settings> {

    override fun apply(target: Settings) {
        target.dependencyResolutionManagement {
            importPackwiz(target.rootDir.resolve("pack/index.toml"))
        }
    }

}