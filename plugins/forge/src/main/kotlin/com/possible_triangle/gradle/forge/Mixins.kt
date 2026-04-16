package com.possible_triangle.gradle.forge

import com.possible_triangle.gradle.features.loaders.addIncluded
import com.possible_triangle.gradle.features.loaders.mainSourceSet
import com.possible_triangle.gradle.features.loaders.mixinExtrasVersion
import com.possible_triangle.gradle.features.resolveDependency
import com.possible_triangle.gradle.mod
import net.neoforged.moddevgradle.legacyforge.dsl.MixinExtension
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.the

internal fun Project.configureMixins() {
    val config = the<ForgeExtension>() as ForgeExtensionImpl

    if (config.mixinsEnabled) {
        configure<MixinExtension> {
            add(mainSourceSet, "${mod.id.get()}.refmap.json")
            config("${mod.id.get()}.mixins.json")
        }

        tasks.named<Jar>("jar") {
            val configs = project.the<MixinExtension>().configs.get()
            manifest.attributes(
                mapOf(
                    "MixinConfigs" to configs.joinToString(","),
                )
            )
        }
    }
}

internal fun Project.mixinExtrasVersion(): String? {
    val config = the<ForgeExtension>() as ForgeExtensionImpl
    return project.mixinExtrasVersion.takeIf { config.mixinsEnabled }
}

internal fun Project.includeMixinExtras(version: String) {
    dependencies {
        val annotationProcessor = add("annotationProcessor", "io.github.llamalad7:mixinextras-common:${version}")
        add("compileOnly", annotationProcessor!!)
        add("implementation", addIncluded(resolveDependency("io.github.llamalad7:mixinextras-forge:${version}")))
    }
}