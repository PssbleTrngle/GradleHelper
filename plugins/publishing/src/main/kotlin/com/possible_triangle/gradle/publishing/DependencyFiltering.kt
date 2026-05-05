package com.possible_triangle.gradle.publishing

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

data class DependencyFilter(
    val groupId: String? = null,
    val artifactId: String? = null,
    val version: String? = null,
    val scope: String? = null,
)

fun Project.removeRuntimeDependencies(publication: MavenPublication) {
    // removeRuntimeModuleDependencies()
    publication.removeRuntimePomDependencies()
}

fun Project.removeDependencies(publication: MavenPublication) {
    // removeModuleDependencies()
    publication.removePomDependencies()
}

fun Project.removeDependencies(
    publication: MavenPublication,
    filter: DependencyFilter,
) {
    // removeModuleDependencies(filter)
    publication.removePomDependencies(filter)
}
