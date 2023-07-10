package com.possible_triangle.gradle.features.loaders

import com.possible_triangle.gradle.mod
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.spongepowered.gradle.vanilla.MinecraftExtension
import org.spongepowered.gradle.vanilla.VanillaGradle

interface CommonExtension : LoaderExtension {
    var minecraftVersion: String?
}

private class CommonExtensionImpl(project: Project) : LoaderExtensionImpl(project), CommonExtension {
    override var minecraftVersion: String? = project.mod.minecraftVersion.orNull
}

fun Project.common(block: CommonExtension.() -> Unit) {
    apply<VanillaGradle>()

    val config = CommonExtensionImpl(this).apply(block)

    configure<MinecraftExtension> {
        version(config.minecraftVersion)
    }

    dependencies {
        add("compileOnly", "org.spongepowered:mixin:0.8.5")

        config.dependsOn.forEach {
            add("implementation", it)
        }
    }
}