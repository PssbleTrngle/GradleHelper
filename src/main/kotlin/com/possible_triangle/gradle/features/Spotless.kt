package com.possible_triangle.gradle.features

import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

fun Project.configureSpotless(enableHook: Boolean, block: SpotlessExtension.() -> Unit) {
    allprojects {
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

            json {
                target("src/main/**/*.json")
                gson().indentWithSpaces(2)
            }

            block()
        }
    }

    if (enableHook) {
        val applyTask = tasks.findByName("spotlessCheck")
        tasks.findByName("preCommit")?.dependsOn(applyTask)
    }
}