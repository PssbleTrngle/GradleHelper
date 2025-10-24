package com.possible_triangle.gradle.upload

import com.modrinth.minotaur.TaskModrinthSyncBody
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import java.io.File
import com.modrinth.minotaur.ModrinthExtension as MinotaurExtension

interface ModrinthExtension : AbstractUploadExtension<SimpleDependencyBuilder> {
    fun syncBodyFrom(file: File)
    fun syncBodyFrom(file: RegularFile) = syncBodyFrom(file.asFile)
    fun syncBodyFromReadme()
}

internal class ModrinthExtensionImpl(private val project: Project) :
    AbstractUploadExtensionImpl<SimpleDependencyBuilder>(project, "modrinth"), ModrinthExtension {
    private val syncFile: RegularFileProperty =
        project.objects.fileProperty()
    private val readmeFile = project.rootProject.file("README.md")

    override fun syncBodyFrom(file: File) {
        syncFile.set(file)
    }

    override fun syncBodyFromReadme() = syncBodyFrom(readmeFile)

    override val dependencies = SimpleDependencyBuilder()

    override fun setup() {
        project.configure<MinotaurExtension> {
            token.set(this@ModrinthExtensionImpl.token)
            projectId.set(this@ModrinthExtensionImpl.projectId)
            versionNumber.set(this@ModrinthExtensionImpl.version)
            versionName.set(this@ModrinthExtensionImpl.versionName)
            changelog.set(this@ModrinthExtensionImpl.changelog)
            gameVersions.set(this@ModrinthExtensionImpl.minecraftVersions)
            loaders.set(modLoaders.map { values -> values.map { it.name.lowercase() } })
            versionType.set(this@ModrinthExtensionImpl.releaseType)
            file.set(this@ModrinthExtensionImpl.file)

            this@ModrinthExtensionImpl.dependencies.required.forEach { required.project(it) }
            this@ModrinthExtensionImpl.dependencies.optional.forEach { optional.project(it) }
            this@ModrinthExtensionImpl.dependencies.embedded.forEach { embedded.project(it) }

            syncFile.orNull?.let {
                syncBodyFrom.set(it.asFile.readText())
            }
        }

        if (isConfigured()) {
            val task = project.tasks.getByName("modrinth")
            project.tasks.publish.dependsOn(task)
            if (syncFile.isPresent) {
                task.dependsOn(project.tasks.withType<TaskModrinthSyncBody>())
            }
        }
    }
}