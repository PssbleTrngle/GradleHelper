package com.possible_triangle.gradle.forge

import com.possible_triangle.gradle.features.loaders.addIncluded
import com.possible_triangle.gradle.features.loaders.mainSourceSet
import com.possible_triangle.gradle.features.loaders.mixinExtrasVersion
import com.possible_triangle.gradle.mod
import net.neoforged.moddevgradle.legacyforge.dsl.MixinExtension
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.filter
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType

internal fun Project.configureMixins() {
    val config = the<ForgeExtension>() as ForgeExtensionImpl

    if (config.mixinsEnabled) {
        includeMixinExtras(project.mixinExtrasVersion)

        tasks.withType<Jar> {
            filesMatching("${mod.id.get()}*.mixins.json") {
                filter(AddMixinRefmap::class, "name" to "${mod.id.get()}.refmap.json")
            }
        }

        configure<MixinExtension> {
            add(mainSourceSet, "${mod.id.get()}.refmap.json")
            config("${mod.id.get()}.mixins.json")
        }

        tasks.named<Jar>("jar") {
            val configs = project.the<MixinExtension>().configs.get()
            manifest.attributes(
                mapOf(
                    "MixinConfigs" to configs.joinToString(","),
                ),
            )
        }
    }
}

internal fun Project.includeMixinExtras(version: String) {
    dependencies {
        val annotationProcessor = add("annotationProcessor", "io.github.llamalad7:mixinextras-common:$version")
        add("compileOnly", annotationProcessor!!)
        add("implementation", "io.github.llamalad7:mixinextras-forge:$version")
        addIncluded("io.github.llamalad7:mixinextras-forge:$version")
    }
}
