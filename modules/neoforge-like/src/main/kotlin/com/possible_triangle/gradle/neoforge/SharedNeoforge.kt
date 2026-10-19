package com.possible_triangle.gradle.neoforge

import com.possible_triangle.gradle.JVM_ARGUMENTS
import com.possible_triangle.gradle.ModVersionProperties
import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.features.loaders.ModLoader
import com.possible_triangle.gradle.features.loaders.displayName
import com.possible_triangle.gradle.features.loaders.mainSourceSet
import com.possible_triangle.gradle.mod
import net.neoforged.moddevgradle.dsl.ModDevExtension
import net.neoforged.moddevgradle.internal.utils.VersionCapabilitiesInternal
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.withType

private fun Project.hasSplitDataRuns(): Boolean {
    val version =
        mod.minecraftVersion
            .map {
                VersionCapabilitiesInternal.ofMinecraftVersion(it)
            }.getOrElse(
                VersionCapabilitiesInternal.latest(),
            )
    return version.splitDataRuns()
}

fun ModDevExtension.sharedConfiguration(project: Project) {
    validateAccessTransformers = true

    project.tasks.withType<Jar> {
        exclude("**/*.accesswidener")
        exclude("**/*.classtweaker")
    }
}

fun Project.kotlinForForge(version: Provider<String>) {
    val modImpl = mod as ModVersionProperties
    lazyDependencies("api") {
        version.orNull?.let { version ->
            val name =
                when (val loader = modImpl.loader.get()) {
                    "neoforge" -> "kotlinforforge-$loader"
                    "forge" -> "kotlinforforge"
                    else -> error("loader '$loader' is not supported by kotlin for forge")
                }
            add("thedarkcolour:$name:$version")
        }
    }
}

fun ModDevExtension.configureModRuns(
    project: Project,
    loader: ModLoader,
) {
    mods.create(project.mod.id.get()) {
        sourceSet(project.mainSourceSet)
    }

    runs {
        create("client") {
            gameDirectory = project.file("run")
            client()
        }

        create("server") {
            gameDirectory = project.file("run/server")
            programArgument("--nogui")
            server()
        }

        create("data") {
            if (project.hasSplitDataRuns()) {
                clientData()
            } else {
                data()
            }
        }

        forEach { run ->
            run.jvmArguments.addAll(JVM_ARGUMENTS)
            if (project.parent != null) {
                run.ideFolderName = project.name
                run.ideName = "${project.name.capitalized()} ${run.name.capitalized()}"
            } else {
                run.ideName = "${loader.displayName()} ${run.name.capitalized()}"
            }
        }
    }
}

fun ModDevExtension.configureParchment(
    project: Project,
    version: Provider<String>,
) {
    version.orNull?.let {
        parchment {
            minecraftVersion = project.mod.minecraftVersion.get()
            mappingsVersion = it
        }
    }
}
