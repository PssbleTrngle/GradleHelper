package com.possible_triangle.gradle.features

import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

fun Project.enableSpotless(enableHook: Boolean) {
    apply<SpotlessPlugin>()

    configure<SpotlessExtension> {
        kotlin {
            ktlint()

            leadingTabsToSpaces()

            suppressLintsFor { shortCode = "standard:package-name" }
            suppressLintsFor { shortCode = "standard:no-wildcard-imports" }
        }

        java {
            importOrder()
            removeUnusedImports()

            leadingTabsToSpaces()
        }

        kotlinGradle {
            ktlint()

            suppressLintsFor { shortCode = "standard:property-naming" }
        }
    }

    if(enableHook) configure<GitExtension> {
        preCommit("spotlessApply")
    }
}