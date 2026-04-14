package com.possible_triangle.gradle.architectury

import com.possible_triangle.gradle.commonMixinDependencies
import com.possible_triangle.gradle.create
import com.possible_triangle.gradle.features.lazyDependencies
import com.possible_triangle.gradle.features.loaders.LoaderPlugin
import com.possible_triangle.gradle.features.loaders.TransparentLoaderSpecifics
import com.possible_triangle.gradle.mod
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the

class GradleHelperArchitecturyPlugin : LoaderPlugin(TransparentLoaderSpecifics) {

    override fun Project.setup() {
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
            }
        }

        tasks.register("prepareWorkspace") {
            doFirst {
                logger.info("Somehow this task is needed")
            }
        }
    }
}
