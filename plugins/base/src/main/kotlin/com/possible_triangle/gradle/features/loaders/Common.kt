package com.possible_triangle.gradle.features.loaders

import com.possible_triangle.gradle.features.lazyDependencies
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import com.possible_triangle.gradle.mod
import org.spongepowered.gradle.vanilla.MinecraftExtension
import org.spongepowered.gradle.vanilla.VanillaGradle

interface CommonExtension : LoaderExtension {
    var minecraftVersion: String?
    var applyVanillaGradle: Boolean
}

private class CommonExtensionImpl(project: Project) : LoaderExtensionImpl(project), CommonExtension {
    override var minecraftVersion: String? = project.mod.minecraftVersion.orNull
    override var applyVanillaGradle: Boolean = true
}

fun Project.setupCommon(block: CommonExtension.() -> Unit) {
    val config = CommonExtensionImpl(this).apply(block)

    if (config.applyVanillaGradle) {
        apply<VanillaGradle>()
        configure<MinecraftExtension> {
            version().set(provider {
                config.minecraftVersion ?: throw IllegalStateException("minecraft version missing")
            })
        }
    }

    dependencies {
        add("compileOnly", "org.spongepowered:mixin:0.8.5")
        mixinExtrasVersion?.also {
            add("compileOnly", "io.github.llamalad7:mixinextras-common:${it}")
        }

        lazyDependencies("implementation") {
            config.dependsOn.forEach {
                add(it)
            }

            config.includedLibraries.forEach {
                add(it)
            }
        }
    }
}

internal val Project.defaultDataGenProject get() = if (isSubProject) findProject(":common") else this

internal val Project.datagenOutput get() = file("src/generated/resources")

internal val Project.existingResources
    get() = listOfNotNull(
        defaultDataGenProject?.file("src/main/resources"),
        file("src/main/resources")
    )

interface DatagenBuilder {
    var owner: Project?
}

fun DatagenBuilder.requireOwner() = requireNotNull(owner) {
    "could not locate default :common project, datagen owner must be configured manually"
}

internal fun Project.configureDatagen() {
    mainSourceSet.resources {
        srcDir(datagenOutput)
    }
}

val JVM_ARGUMENTS = listOf("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")