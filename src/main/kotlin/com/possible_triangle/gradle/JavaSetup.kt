package com.possible_triangle.gradle

import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
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
                    "Specification-Title" to mod.name.orElse(mod.id).orNull,
                    "Specification-Vendor" to mod.author.orNull,
                    "Specification-Version" to mod.version.orNull,
                    "Implementation-Title" to name,
                    "Implementation-Version" to archiveVersion,
                    "Implementation-Vendor" to mod.author.orNull,
                    "Implementation-Timestamp" to now,
                ).filterValues { it != null }
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