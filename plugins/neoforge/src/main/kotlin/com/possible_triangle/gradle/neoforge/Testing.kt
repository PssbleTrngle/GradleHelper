package com.possible_triangle.gradle.neoforge

import com.possible_triangle.gradle.mod
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.the

internal fun Project.setupJUnit() {
    val config = the<NeoforgeExtension>()

    tasks.named<Test>("test") {
        useJUnitPlatform()
    }

    configure<NeoForgeExtension> {
        unitTest {
            enable()
            testedMod = mod.id.map { mods[it] }
        }
    }

    dependencies {
        add("testImplementation", "org.junit.jupiter:junit-jupiter:5.7.1")
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
        add("testImplementation", "net.neoforged:testframework:${config.neoforgeVersion.get()}")
    }
}
