package com.possible_triangle.gradle

import com.possible_triangle.gradle.features.loaders.isSubProject
import com.possible_triangle.gradle.features.loaders.mainSourceSet
import org.gradle.api.Project

val JVM_ARGUMENTS = listOf("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")

val Project.defaultDataGenProject get() = if (isSubProject) findProject(":common") else this

val Project.datagenOutput get() = file("src/generated/resources")

val Project.existingResources
    get() = listOfNotNull(
        defaultDataGenProject?.file("src/main/resources"),
        file("src/main/resources")
    )

interface DatagenBuilder {
    var owner: Project?
    fun existing(vararg mods: String)
}

fun DatagenBuilder.requireOwner() = requireNotNull(owner) {
    "could not locate default :common project, datagen owner must be configured manually"
}

fun Project.configureDatagen() {
    mainSourceSet.resources {
        srcDir(datagenOutput)
    }
}