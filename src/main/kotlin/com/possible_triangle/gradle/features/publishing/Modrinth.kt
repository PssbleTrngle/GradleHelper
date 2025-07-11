package com.possible_triangle.gradle.features.publishing

import com.modrinth.minotaur.Minotaur
import com.modrinth.minotaur.TaskModrinthSyncBody
import com.possible_triangle.gradle.features.loaders.ModLoader
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import java.io.File
import com.modrinth.minotaur.ModrinthExtension as MinotaurExtension

interface ModrinthExtension : UploadExtension {
    fun syncBodyFrom(file: File)
    fun syncBodyFrom(file: RegularFile) = syncBodyFrom(file.asFile)
    fun syncBodyFromReadme()
}

private class ModrinthExtensionImpl(project: Project, private val syncFile: RegularFileProperty) :
    UploadExtensionImpl(project, "modrinth"), ModrinthExtension {
    private val readmeFile = project.rootProject.file("README.md")

    override fun syncBodyFrom(file: File) {
        syncFile.set(file)
    }

    override fun syncBodyFromReadme() = syncBodyFrom(readmeFile)

    override fun DependencyBuilder.requireKotlin(loader: ModLoader) {
        when (loader) {
            ModLoader.FORGE, ModLoader.NEOFORGE -> required("ordsPcFz")
            ModLoader.FABRIC -> required("Ha28R6CL")
        }
    }
}

fun Project.enableMinotaur(block: ModrinthExtension.() -> Unit) {
    apply<Minotaur>()

    val syncFile = project.objects.fileProperty()
    val uploadInfo = ModrinthExtensionImpl(this, syncFile).apply(block).buildIfToken() ?: return run {
        logger.warn("No modrinth token set, minotaur will not be configured")
    }

    configure<MinotaurExtension> {
        token.set(uploadInfo.token)
        projectId.set(uploadInfo.projectId)
        versionNumber.set(uploadInfo.version)
        versionName.set(uploadInfo.versionName)
        changelog.set(uploadInfo.changelog)
        gameVersions.set(uploadInfo.minecraftVersions)
        loaders.set(uploadInfo.modLoaders)
        versionType.set(uploadInfo.releaseType)
        file.set(uploadInfo.file)

        uploadInfo.requiredDependencies.forEach { required.project(it) }
        uploadInfo.optionalDependencies.forEach { optional.project(it) }
        uploadInfo.embeddedDependencies.forEach { embedded.project(it) }

        syncFile.orNull?.let {
            syncBodyFrom.set(it.asFile.readText())
        }
    }

    val task = tasks.getByName("modrinth")
    tasks.publish.dependsOn(task)
    if(syncFile.isPresent) {
        task.dependsOn(tasks.withType<TaskModrinthSyncBody>())
    }
}