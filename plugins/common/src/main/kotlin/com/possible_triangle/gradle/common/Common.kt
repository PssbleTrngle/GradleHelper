package com.possible_triangle.gradle.common

import com.possible_triangle.gradle.GradleHelperCorePlugin
import com.possible_triangle.gradle.commonMixinDependencies
import com.possible_triangle.gradle.create
import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.mod
import net.neoforged.moddevgradle.boot.ModDevPlugin
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class GradleHelperCommonPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.apply<GradleHelperCorePlugin>()
        target.setupCommon()
    }

    private fun Project.setupCommon() {
        val config = extensions.create<CommonExtension, CommonExtensionImpl>("common")

        apply<ModDevPlugin>()

        afterEvaluate {
            configure<NeoForgeExtension> {
                neoFormVersion = config.neoformVersion.get()

                runs.removeAll { true }
            }
        }

        commonMixinDependencies()

        dependencies {
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
