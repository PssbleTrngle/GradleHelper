package com.possible_triangle.gradle.upload

import com.modrinth.minotaur.Minotaur
import com.possible_triangle.gradle.coreProject
import com.possible_triangle.gradle.create
import com.possible_triangle.gradle.getOrCreate
import com.possible_triangle.gradle.publishing.GradleHelperPublishingPluginInternal
import com.possible_triangle.gradle.releaseMetadata
import net.darkhax.curseforgegradle.CurseForgeGradlePlugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.the

interface UploadExtension {
    val modrinth: ModrinthExtension
    val curseforge: CurseForgeExtension
    val maven: ModMavenPublishingExtension

    fun maven(block: ModMavenPublishingExtension.() -> Unit)

    fun curseforge(block: CurseForgeExtension.() -> Unit)

    fun modrinth(block: ModrinthExtension.() -> Unit)

    fun forEach(block: AbstractUploadExtension<*>.() -> Unit)
}

internal open class UploadExtensionImpl(
    project: Project,
) : UploadExtension {
    override val modrinth = ModrinthExtensionImpl(project)
    override val curseforge = CurseForgeExtensionImpl(project)
    override val maven = ModMavenPublishingExtensionImpl(project)

    override fun maven(block: ModMavenPublishingExtension.() -> Unit) = maven.block()

    override fun curseforge(block: CurseForgeExtension.() -> Unit) {
        curseforge.block()
    }

    override fun modrinth(block: ModrinthExtension.() -> Unit) {
        modrinth.block()
    }

    override fun forEach(block: AbstractUploadExtension<*>.() -> Unit) {
        curseforge(block)
        modrinth(block)
    }

    fun setup() {
        modrinth.setup()
        curseforge.setup()
        maven.setup()
    }
}

private const val TASK_NAME = "upload"

internal fun Project.registerUpload() {
    extensions.create<UploadExtension, UploadExtensionImpl>("upload")
    tasks.register(TASK_NAME)
}

internal val TaskContainer.upload
    get() = getByName<Task>(TASK_NAME)

internal fun Project.modifyUploadTask(
    name: String,
    block: Task.() -> Unit,
) {
    coreProject.tasks
        .getOrCreate<Task>(name) {
            coreProject.tasks.releaseMetadata.dependsOn(this)
            coreProject.tasks.upload.dependsOn(this)
        }.block()
}

internal fun Project.configureUpload() {
    apply<GradleHelperPublishingPluginInternal>()
    apply<CurseForgeGradlePlugin>()
    apply<Minotaur>()

    val upload = the<UploadExtension>() as UploadExtensionImpl

    project.afterEvaluate {
        upload.setup()
    }
}
