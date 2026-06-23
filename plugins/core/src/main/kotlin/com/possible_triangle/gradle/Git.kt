package com.possible_triangle.gradle

import org.gradle.api.Project

fun Project.gitCommitHash(): String {
    try {
        val output =
            providers.exec {
                commandLine("git", "rev-parse", "HEAD")
            }
        return output.standardOutput.asText
            .get()
            .trim()
    } catch (ignored: Throwable) {
        throw RuntimeException("unable to calculate git hash", ignored)
    }
}
