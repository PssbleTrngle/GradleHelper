package com.possible_triangle.gradle.common

import com.possible_triangle.gradle.GradleHelperCorePlugin
import com.possible_triangle.gradle.create
import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.mod
import net.neoforged.gradle.common.CommonProjectPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

class GradleHelperCommonPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.apply<GradleHelperCorePlugin>()
        target.setupCommon()
    }

    private fun Project.setupCommon() {
        val config = extensions.create<CommonExtension, CommonExtensionImpl>("common")

        apply<CommonProjectPlugin>()

        dependencies {
            //add("compileOnly", "org.spongepowered:mixin:0.8.5")
            //mixinExtrasVersion?.also {
            //    add("compileOnly", "io.github.llamalad7:mixinextras-common:${it}")
            //}

            add("implementation", mod.minecraftVersion.map {
                "net.minecraft:client:$it:client-extra"
            })

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
