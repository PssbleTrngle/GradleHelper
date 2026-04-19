package com.possible_triangle.gradle.vanilla

import com.possible_triangle.gradle.commonMixinDependencies
import com.possible_triangle.gradle.create
import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.features.loaders.LoaderPlugin
import com.possible_triangle.gradle.features.loaders.TransparentLoaderSpecifics
import com.possible_triangle.gradle.mod
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.spongepowered.gradle.vanilla.MinecraftExtension
import org.spongepowered.gradle.vanilla.VanillaGradle

class GradleHelperVanillaPlugin : LoaderPlugin(TransparentLoaderSpecifics) {

    override fun Project.setup() {
        val config = extensions.create<CommonExtension, CommonExtensionImpl>("common")

        apply<VanillaGradle>()
        configure<MinecraftExtension> {
            version().set(mod.minecraftVersion)
        }

        commonMixinDependencies()

        dependencies {
            lazyDependencies("implementation") {
                config.dependsOn.forEach {
                    add(it)
                }
            }
        }
    }
}
