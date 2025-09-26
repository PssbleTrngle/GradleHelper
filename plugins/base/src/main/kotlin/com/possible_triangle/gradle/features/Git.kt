package com.possible_triangle.gradle.features

import com.possible_triangle.gradle.GradleHelperPlugin
import org.gradle.api.Project
import java.io.File

private val hook = File(".git/hooks/pre-commit")
private val backup = File(".git/hooks/pre-commit.bak")

fun Project.enableHook() {
    val resource = GradleHelperPlugin::class.java.classLoader.getResource("hooks/pre-commit.sh")
        ?: throw NullPointerException("Unable to find pre commit hook")
    val text = resource.readText()

    tasks.register("preCommit") {
        group = "git"
    }

    if (hook.exists()) {
        hook.copyTo(backup)
    }

    if (hook.parentFile.exists()) hook.writeText(text)
    else logger.warn("could not locate git directory at ${hook.parentFile.parentFile.absoluteFile}")
}

fun Project.disableHook() {
    hook.delete()

    if (backup.exists()) {
        backup.copyTo(hook)
        backup.delete()
    }

    tasks.findByName("preCommit")?.let {
        tasks.remove(it)
    }
}

fun Project.registerHookTasks() {
    tasks.register("enableHook") {
        group = "git"
        doLast {
            enableHook()
        }
    }

    tasks.register("disableHook") {
        group = "git"
        doLast {
            disableHook()
        }
    }
}