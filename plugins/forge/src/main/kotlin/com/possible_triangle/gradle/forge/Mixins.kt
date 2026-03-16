package com.possible_triangle.gradle.forge

import com.possible_triangle.gradle.features.loaders.mixinExtrasVersion
import com.possible_triangle.gradle.mod
import net.minecraftforge.renamer.gradle.RenamerExtension
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*

internal fun Project.configureMixins() {
    val config = the<ForgeExtension>() as ForgeExtensionImpl

    if (config.mixinsEnabled) {
        configure<RenamerExtension>() {
            enableMixinRefmaps {
                config("${mod.id.get()}.refmap.json")
            }

            classes(tasks.getByName<Jar>("jar")) {
                mappings(mixin.generatedMappings)
            }
        }

        // TODO check
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