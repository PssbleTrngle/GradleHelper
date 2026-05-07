package com.possible_triangle.gradle.features.loaders

import com.possible_triangle.gradle.GradleHelperCorePlugin
import com.possible_triangle.gradle.configureJarTasks
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.resolveProperties
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources

abstract class LoaderPlugin(
    private val loaderSpecifics: LoaderSpecifics,
) : Plugin<Project> {
    final override fun apply(target: Project) {
        target.registerLoaderSpecifics(loaderSpecifics)
        target.apply<GradleHelperCorePlugin>()

        target.provideProperties()
        target.configureJarTasks()

        target.setup()

        target.tasks.withType<Test> { onlyIf { target.testEnabled() } }
        target.tasks.findByName("compileTestJava")?.onlyIf { target.testEnabled() }
        target.tasks.findByName("compileTestKotlin")?.onlyIf { target.testEnabled() }

        target.afterEvaluate { finalize() }
    }

    protected abstract fun Project.setup()

    protected open fun Project.finalize() {
    }

    private fun Project.provideProperties() {
        @Suppress("UnstableApiUsage")
        tasks.withType<ProcessResources> {
            // this will ensure that this task is redone when the versions change.
            inputs.property("version", mod.version)

            filesMatching(mod.additional.files.get()) {
                expand(mod.resolveProperties())
            }
        }
    }

    protected open fun Project.testEnabled(): Boolean = false
}
