package com.possible_triangle.gradle

import com.possible_triangle.gradle.upload.publish
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import org.gradle.work.DisableCachingByDefault

@Serializable
abstract class ReleaseMetadata {
    abstract val tag: Property<String>
    abstract val modrinthUrl: Property<String>
    abstract val curseforgeUrl: Property<String>
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
        val encoded = JSON.encodeToString(releases.names.associateWith { releases.getByName(it) })
        getOutput().get().asFile.writeText(encoded)
    }
}

private const val TASK_NAME = "generateReleaseMetadata"

val Project.releaseMetadataTask: GenerateReleaseMetadataTask
    get() = coreProject.tasks.getByName<GenerateReleaseMetadataTask>(TASK_NAME)

fun Project.setupReleaseMetadata() {
    if (this == coreProject) {
        val releaseMetadataTask =
            tasks.register<GenerateReleaseMetadataTask>(TASK_NAME) {
                getOutput().convention(project.layout.buildDirectory.file("release.json"))
            }
        tasks.publish.finalizedBy(releaseMetadataTask)
    }

    if (subprojects.isEmpty()) {
        createReleaseMetadata()
    }
}

fun Project.createReleaseMetadata() {
    releaseMetadataTask.apply {
        releases.create(project.name) {
            tag.convention(project.mod.version)
        }
    }
}

fun Project.modifyReleaseMetadata(block: ReleaseMetadata.() -> Unit = {}) {
    releaseMetadataTask.releases.named(project.name, block)
}
