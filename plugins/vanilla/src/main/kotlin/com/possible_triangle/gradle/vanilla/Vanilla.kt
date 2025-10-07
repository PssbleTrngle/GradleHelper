package com.possible_triangle.gradle.vanilla

import com.possible_triangle.gradle.GradleHelperCorePlugin
import com.possible_triangle.gradle.create
import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.features.loaders.mixinExtrasVersion
import com.possible_triangle.gradle.mod
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.spongepowered.gradle.vanilla.MinecraftExtension
import org.spongepowered.gradle.vanilla.VanillaGradle

class GradleHelperVanillaPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.apply<GradleHelperCorePlugin>()
        target.setupCommon()
    }

    private fun Project.setupCommon() {
        val config = extensions.create<CommonExtension, CommonExtensionImpl>("common")

        apply<VanillaGradle>()
        configure<MinecraftExtension> {
            version().set(mod.minecraftVersion)
        }

        dependencies {
            add("compileOnly", "org.spongepowered:mixin:0.8.5")
            mixinExtrasVersion?.also {
                add("compileOnly", "io.github.llamalad7:mixinextras-common:${it}")
            }

            lazyDependencies("implementation") {
                config.dependsOn.forEach {
                    add(it)
                }

                mod.libraries.get().forEach {
                    add(it)
                }
            }
        }
    }
}
