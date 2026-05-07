package com.possible_triangle.gradle

import com.possible_triangle.gradle.upload.upload
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import org.gradle.work.DisableCachingByDefault
import kotlin.collections.mapValues

interface ReleaseMetadata {
    val name: String
    val tag: Property<String>
    val modrinthUrl: Property<String>
    val curseforgeUrl: Property<String>
}

@Serializable
data class SerializedReleaseMetadata(
    val tag: String? = null,
    val modrinthUrl: String? = null,
    val curseforgeUrl: String? = null,
) {
    companion object {
        fun from(value: ReleaseMetadata) =
            SerializedReleaseMetadata(
                tag = value.tag.orNull,
                modrinthUrl = value.modrinthUrl.orNull,
                curseforgeUrl = value.curseforgeUrl.orNull,
            )
    }
}

private val JSON =
    Json {
        prettyPrint = true
    }

@DisableCachingByDefault
abstract class GenerateReleaseMetadataTask : DefaultTask() {
    @OutputFile
    abstract fun getOutput(): RegularFileProperty

    @get:Input
    abstract val releases: NamedDomainObjectContainer<ReleaseMetadata>

    @TaskAction
    fun generate() {
        if (releases.isEmpty()) return
        val encoded =
            JSON.encodeToString(
                releases
                    .associateBy { it.name }
                    .mapValues { SerializedReleaseMetadata.from(it.value) },
            )
        getOutput().get().asFile.writeText(encoded)
    }
}

private const val TASK_NAME = "generateReleaseMetadata"

val TaskContainer.releaseMetadata: GenerateReleaseMetadataTask
    get() = getByName<GenerateReleaseMetadataTask>(TASK_NAME)

fun Project.setupReleaseMetadata() {
    if (this == coreProject) {
        val releaseMetadataTask =
            tasks.register<GenerateReleaseMetadataTask>(TASK_NAME) {
                getOutput().convention(project.layout.buildDirectory.file("release.json"))
            }

        tasks.getOrCreate<Task>("publish") {
            finalizedBy(releaseMetadataTask)
            tasks.upload.finalizedBy(this)
        }
    }
}

internal fun Project.createReleaseMetadata() {
    coreProject.tasks.releaseMetadata.releases.create(project.name) {
        tag.convention(project.mod.version)
    }
}

fun Project.modifyReleaseMetadata(block: ReleaseMetadata.() -> Unit = {}) {
    coreProject.tasks.releaseMetadata.releases
        .named(project.name, block)
}
