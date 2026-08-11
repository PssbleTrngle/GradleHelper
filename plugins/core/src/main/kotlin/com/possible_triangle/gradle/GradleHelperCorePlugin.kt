package com.possible_triangle.gradle

import com.possible_triangle.gradle.repositories.defaultRepositories
import com.possible_triangle.gradle.upload.setupUpload
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.withType
import kotlin.text.get

class GradleHelperCorePlugin : Plugin<Project> {
    override fun apply(target: Project) = target.configure()

    private fun Project.configure() {
        loadEnv()

        logger.lifecycle("applying gradle helper ${BuildParameters.PLUGIN_VERSION} in ${project.name}")

        project.createModExtension()

        repositories {
            defaultRepositories()
        }

        setupJava()
        configureBaseName()
        setupUpload()
        setupReleaseMetadata()

        tasks.withType<Jar> {
            exclude(".cache")
            exclude("**/*.bbmodel")
            exclude("**/*.aseprite")
            exclude("**/*.xcf")
        }

        // needed so compile-only dependencies are also available for tests
        configurations.named("testCompileOnly") {
            extendsFrom(configurations.getByName("compileOnly"))
        }

        tasks.withType<Test> {
            exclude("**/mixins/**")
            exclude("**/mixin/**")
        }
    }
}
