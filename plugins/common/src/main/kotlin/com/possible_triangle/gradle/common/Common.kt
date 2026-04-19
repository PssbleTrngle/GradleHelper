package com.possible_triangle.gradle.common

import com.possible_triangle.gradle.commonMixinDependencies
import com.possible_triangle.gradle.create
import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.features.loaders.LoaderPlugin
import com.possible_triangle.gradle.features.loaders.TransparentLoaderSpecifics
import com.possible_triangle.gradle.mod
import net.neoforged.moddevgradle.boot.ModDevPlugin
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class GradleHelperCommonPlugin : LoaderPlugin(TransparentLoaderSpecifics) {

    override fun Project.setup() {
        val config = extensions.create<CommonExtension, CommonExtensionImpl>("common")

        apply<ModDevPlugin>()

        configure<NeoForgeExtension> {
            config.parchmentMappingsVersion.orNull?.let {
                parchment {
                    minecraftVersion = mod.minecraftVersion.get()
                    mappingsVersion = it
                }
            }
        }

        afterEvaluate {
            configure<NeoForgeExtension> {
                neoFormVersion = config.neoformVersion.get()

                runs.removeAll { true }
            }
        }

        if (tasks.findByName("prepareWorkspace") == null) {
            tasks.register("prepareWorkspace") {
                // I don't know why this is necessary, but for some reason IDEA calls this task after import
            }
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
