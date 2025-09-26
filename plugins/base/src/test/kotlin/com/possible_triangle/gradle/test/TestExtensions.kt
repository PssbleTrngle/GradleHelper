package com.possible_triangle.gradle.test

import com.possible_triangle.gradle.GradleHelperPlugin
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.extra
import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import java.util.*

fun Project.loadProperties(from: String = "gradle.properties") {
    val file = project.projectDir.resolve(from)
    if (!file.exists()) return

    val props = Properties()
    file.inputStream().use { props.load(it) }
    props.forEach { key, value ->
        project.extra[key.toString()] = value
    }
}

fun createProject(block: ProjectBuilder.() -> Unit = {}) = createProjectWithoutPlugin(block).also {
    it.apply<GradleHelperPlugin>()
}

fun createProjectWithoutPlugin(block: ProjectBuilder.() -> Unit = {}): Project {
    val project = ProjectBuilder.builder()
        .withGradleUserHomeDir(File(".gradle/userHome"))
        .apply(block)
        .build()

    project.loadProperties()
    return project
}

fun ProjectBuilder.withProjectDir(name: String) =
    withProjectDir(File("src/test/resources/projects").resolve(name))

fun Project.findTestDependencies(type: String, group: String = "test.something"): DomainObjectSet<Dependency> {
    val configuration = configurations.getByName(type)
    return configuration.incoming.dependencies.matching {
        it.group == group
    }
}