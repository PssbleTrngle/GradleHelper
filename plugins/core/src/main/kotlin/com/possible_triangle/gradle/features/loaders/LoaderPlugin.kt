package com.possible_triangle.gradle.features.loaders

import com.possible_triangle.gradle.GradleHelperCorePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

abstract class LoaderPlugin(
    private val loaderSpecifics: LoaderSpecifics,
) : Plugin<Project> {
    final override fun apply(target: Project) {
        target.registerLoaderSpecifics(loaderSpecifics)
        target.apply<GradleHelperCorePlugin>()
        target.setup()
        target.afterEvaluate { finalize() }
    }

    protected abstract fun Project.setup()

    protected open fun Project.finalize() {
    }
}
