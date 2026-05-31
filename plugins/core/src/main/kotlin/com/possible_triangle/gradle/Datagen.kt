package com.possible_triangle.gradle

import com.possible_triangle.gradle.features.loaders.isSubProject
import com.possible_triangle.gradle.features.loaders.mainSourceSet
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import java.io.File

val JVM_ARGUMENTS = listOf("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")

internal val Project.defaultDataGenProject get() = if (isSubProject) project(":common") else this

internal val Project.datagenOutput get() = file("src/generated/resources")

internal val Project.existingResources
    get() =
        setOf(
            defaultDataGenProject.file("src/main/resources"),
            file("src/main/resources"),
        )

interface DatagenBuilder {
    val owner: Property<Project>

    fun existing(vararg mods: String)

    fun splitSourceSet(name: String = "data")

    fun sourceSet(sourceSet: Provider<SourceSet>)
}

internal fun Project.configureDatagen(
    output: File,
    resourcesConfiguration: String,
) {
    mainSourceSet.resources {
        srcDir(output)
    }

    artifacts {
        add(resourcesConfiguration, output)
    }
}
