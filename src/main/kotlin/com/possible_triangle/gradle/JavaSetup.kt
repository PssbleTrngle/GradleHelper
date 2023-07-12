package com.possible_triangle.gradle

import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.*
import java.time.LocalDateTime

internal fun Project.setupJava() {
    apply<JavaPlugin>()

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
        withSourcesJar()
    }
}

internal fun Project.configureJarTasks() {
    val modName = mod.name.orElse(mod.id)

    tasks.withType<Jar> {
        val now = LocalDateTime.now().toString()

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${modName.get()}" }
        }

        manifest {
            attributes(
                mapOf(
                    "Specification-Title" to modName,
                    "Specification-Vendor" to mod.author,
                    "Specification-Version" to mod.version,
                    "Implementation-Title" to modName,
                    "Implementation-Version" to archiveVersion.orElse(mod.version),
                    "Implementation-Vendor" to mod.author,
                    "Implementation-Timestamp" to now,
                )
            )
        }
    }

    tasks.named<Jar>("sourcesJar") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${modName.get()}" }
        }
    }
}