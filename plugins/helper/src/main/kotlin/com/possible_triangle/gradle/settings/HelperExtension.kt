package com.possible_triangle.gradle.settings

import org.gradle.api.initialization.Settings
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create

interface HelperExtension {
    val versionStrategy: Property<ResolutionStrategy>
}

internal fun Settings.createHelperExtension(): HelperExtension =
    extensions.create<HelperExtension>("helper").apply {
        versionStrategy.convention(
            if (BuildParameters.IS_DEV) {
                ResolutionStrategy.SNAPSHOT
            } else {
                ResolutionStrategy.FETCH
            },
        )
    }
