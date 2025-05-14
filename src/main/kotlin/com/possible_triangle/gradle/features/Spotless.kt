package com.possible_triangle.gradle.features

import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import java.io.File

fun Project.enableSpotless() {
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

    tasks.create("initializeHooks") {
        group = "other"

        doLast {
            copyHook("pre-commit")
        }
    }
}

private fun copyHook(name: String) {
    val resource = object {}.javaClass.getResource("hooks/$name.sh")
        ?: throw NullPointerException("Unable to find hook $name")
    val text =  resource.readText()
    File(".git/hooks/$name").writeText(text)
}