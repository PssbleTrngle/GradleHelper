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

fun preCommitHook(tasks: Collection<String>) {
    val resource = GitExtension::class.java.getResource("hooks/pre-commit.sh")
        ?: throw NullPointerException("Unable to find pre commit hook")
    val text = resource.readText().replace("{tasks}", tasks.joinToString(" "))
    File(".git/hooks/pre-commit").writeText(text)
}

fun Project.setupGitExtension() {
    extensions.create(GitExtension::class.java, "git", GitExtensionImpl::class.java)

    tasks.register("initializeHooks") {
        group = "other"

        doLast {
            val extension = project.the<GitExtension>()
            preCommitHook(extension.preCommitTasks)
        }
    }
}