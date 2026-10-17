package com.possible_triangle.gradle

import com.possible_triangle.gradle.upload.baseNameConvention
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import java.time.LocalDateTime

internal fun javaVersionFor(minecraftVersion: String): Int {
    val semVer = SemVer.parse(minecraftVersion)
    if (semVer.major > 1) return 25
    if (semVer.minor > 20) return 21
    return 17
}

private val Project.javaVersion
    get() =
        intProperty("java_version")
            .orElse(project.mod.minecraftVersion.map(::javaVersionFor))

internal fun Project.setupJava() {
    apply<JavaPlugin>()

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(javaVersion.map(JavaLanguageVersion::of))
        }
        withSourcesJar()
        withJavadocJar()
    }
}

internal fun Project.configureJarTasks() {
    val modName = mod.name.orElse(mod.id)

    tasks.withType<Jar> {
        val now = LocalDateTime.now().toString()

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        listOf(".md", ".txt", "").forEach { ext ->
            from(rootProject.file("LICENSE$ext")) {
                rename { "LICENSE_${modName.get()}$ext" }
            }
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
                ),
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

internal fun Project.configureBaseName() {
    configure<BasePluginExtension> {
        archivesName.set(project.baseNameConvention())
    }
}
