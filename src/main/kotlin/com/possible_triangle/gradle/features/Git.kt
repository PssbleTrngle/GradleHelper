package com.possible_triangle.gradle.features

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.the
import java.io.File

interface GitExtension {
    val preCommitTasks: Collection<String>

    fun preCommit(task: String)
    fun preCommit(task: Task) = preCommit(task.name)
    fun preCommit(task: TaskProvider<*>) = preCommit(task.name)
}

open class GitExtensionImpl : GitExtension {
    override val preCommitTasks = mutableSetOf<String>()

    override fun preCommit(task: String) {
        preCommitTasks.add(task)
    }
}

fun preCommitHook() {
    val resource = GitExtension::class.java.getResource("hooks/pre-commit.sh")
        ?: throw NullPointerException("Unable to find pre commit hook")
    val text = resource.readText()
    File(".git/hooks/pre-commit").writeText(text)
}

fun Project.setupGitExtension() {
    extensions.create(GitExtension::class.java, "git", GitExtensionImpl::class.java)

    val extension = project.the<GitExtension>()
    tasks.findByName("check")?.let { task ->
        extension.preCommitTasks.forEach {
            task.dependsOn(it)
        }
    }

    tasks.register("initializeHooks") {
        group = "other"

        doLast {
            preCommitHook()
        }
    }
}