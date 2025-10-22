package com.possible_triangle.gradle.forge

import com.possible_triangle.gradle.features.loaders.mainSourceSet
import com.possible_triangle.gradle.features.loaders.mixinExtrasVersion
import com.possible_triangle.gradle.mod
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.*
import org.spongepowered.asm.gradle.plugins.MixinExtension
import org.spongepowered.asm.gradle.plugins.MixinGradlePlugin

internal fun Project.enableMixins() {
    apply<MixinGradlePlugin>()
}

internal fun Project.configureMixins() {
    val config = the<ForgeExtension>() as ForgeExtensionImpl

    if (config.mixinsEnabled) {
        configure<MixinExtension> {
            add(mainSourceSet, "${mod.id.get()}.refmap.json")
            config("${mod.id.get()}.mixins.json")
        }

        // workaround because of https://github.com/SpongePowered/MixinGradle/issues/48
        tasks.withType<JavaCompile> {
            doFirst {
                options.compilerArgs.replaceAll { it: Any ->
                    it.toString()
                }
            }
        }
    }
}

internal fun Project.includeMixinExtras(): Boolean {
    val config = the<ForgeExtension>() as ForgeExtensionImpl

    return project.mixinExtrasVersion.takeIf { config.mixinsEnabled }?.also {
        dependencies {
            val annotationProcessor = add("annotationProcessor", "io.github.llamalad7:mixinextras-common:${it}")
            add("compileOnly", annotationProcessor!!)
            add("implementation", pin(jarJar, create("io.github.llamalad7", "mixinextras-forge", it)))
        }
    } != null
}