package com.possible_triangle.gradle

import com.possible_triangle.gradle.features.loaders.mixinExtrasVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

fun Project.commonMixinDependencies() {
    dependencies {
        add("compileOnly", "org.spongepowered:mixin:0.8.5")
        add("compileOnly", "org.ow2.asm:asm-tree:9.5")
        mixinExtrasVersion?.also {
            add("compileOnly", "io.github.llamalad7:mixinextras-common:${it}")
        }
    }
}