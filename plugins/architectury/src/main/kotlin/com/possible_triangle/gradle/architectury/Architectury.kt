package com.possible_triangle.gradle.architectury

import com.possible_triangle.gradle.GradleHelperCorePlugin
import com.possible_triangle.gradle.commonMixinDependencies
import com.possible_triangle.gradle.create
import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.mod
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the

class GradleHelperArchitecturyPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.apply<GradleHelperCorePlugin>()
        target.setupCommon()
    }

    private fun Project.setupCommon() {
        val config = extensions.create<CommonExtension, CommonExtensionImpl>("common")

        apply(plugin = "dev.architectury.loom")
        val loom = the<LoomGradleExtensionAPI>()

        commonMixinDependencies()

        dependencies {
            add("minecraft", mod.minecraftVersion.map { "com.mojang:minecraft:$it" })
            add("mappings", loom.officialMojangMappings())

            lazyDependencies("implementation") {
                config.dependsOn.forEach {
                    add(it)
                }

                mod.libraries.get().forEach {
                    add(it)
                }
            }
        }

        tasks.register("prepareWorkspace") {
            doFirst {
                logger.info("Somehow this task is needed")
            }
        }
    }
}
