package com.possible_triangle.gradle.features

import com.possible_triangle.gradle.GradleHelperPlugin
import org.gradle.api.Project
import java.io.File

fun Project.setupGitExtension() {
    val resource = GradleHelperPlugin::class.java.classLoader.getResource("hooks/pre-commit.sh")
        ?: throw NullPointerException("Unable to find pre commit hook")
    val text = resource.readText()
    val to = File(".git/hooks/pre-commit")
    if (to.parentFile.exists()) to.writeText(text)
    else logger.warn("could not locate git directory at ${to.parentFile.parentFile.absoluteFile}")
}