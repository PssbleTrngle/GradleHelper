package com.possible_triangle.gradle.features

import com.possible_triangle.gradle.GradleHelperPlugin
import org.gradle.api.Project
import java.io.File

fun copyPreCommitHook() {
    val resource = GradleHelperPlugin::class.java.classLoader.getResource("hooks/pre-commit.sh")
        ?: throw NullPointerException("Unable to find pre commit hook")
    val text = resource.readText()
    File(".git/hooks/pre-commit").writeText(text)
}

fun Project.setupGitExtension() {
    copyPreCommitHook()
}