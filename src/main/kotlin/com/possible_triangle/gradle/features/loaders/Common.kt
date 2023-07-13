package com.possible_triangle.gradle.features.loaders

import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.mod
import org.spongepowered.gradle.vanilla.MinecraftExtension
import org.spongepowered.gradle.vanilla.VanillaGradle

interface CommonExtension : LoaderExtension {
    var minecraftVersion: String?
}

private class CommonExtensionImpl(project: Project) : LoaderExtensionImpl(project), CommonExtension {
    override var minecraftVersion: String? = project.mod.minecraftVersion.orNull
}

fun Project.setupCommon(block: CommonExtension.() -> Unit) {
    apply<VanillaGradle>()

    val config = CommonExtensionImpl(this).apply(block)

    configure<MinecraftExtension> {
        version().set(provider {
            config.minecraftVersion ?: throw IllegalStateException("minecraft version missing")
        })
    }

    dependencies {
        add("compileOnly", "org.spongepowered:mixin:0.8.5")

        config.dependsOn.forEach {
            add("implementation", it)
        }

        config.includedLibraries.forEach {
            add("implementation", it)
        }
    }
}

internal val Project.dataGenProject get() = if (isSubProject) project(":common") else this

internal val Project.datagenOutput get() = dataGenProject.file("src/generated/resources")

internal fun Project.configureDatagen() {
    dataGenProject.mainSourceSet.resources {
        srcDir(datagenOutput)
    }
}