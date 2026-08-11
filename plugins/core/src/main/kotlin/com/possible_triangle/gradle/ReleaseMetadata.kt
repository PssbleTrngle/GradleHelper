package com.possible_triangle.gradle

import com.possible_triangle.gradle.metadata.fetchMavenMetadata
import com.possible_triangle.gradle.upload.UploadExtension
import com.possible_triangle.gradle.upload.metadataTagConvention
import com.possible_triangle.gradle.upload.upload
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.publish.Publication
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.gradle.work.DisableCachingByDefault
import java.net.URI
import kotlin.collections.mapValues

interface ReleaseMetadata {
    val name: String
    val tag: Property<String>
    val preRelease: Property<Boolean>
    val modrinthUrl: Property<String>
    val curseforgeUrl: Property<String>
    val mavenUrl: Property<String>
}

@Serializable
data class SerializedReleaseMetadata(
    val tag: String? = null,
    val preRelease: Boolean = false,
    val modrinthUrl: String? = null,
    val curseforgeUrl: String? = null,
    val mavenUrl: String? = null,
) {
    companion object {
        fun from(value: ReleaseMetadata) =
            SerializedReleaseMetadata(
                tag = value.tag.orNull,
                preRelease = value.preRelease.getOrElse(false),
                modrinthUrl = value.modrinthUrl.orNull,
                curseforgeUrl = value.curseforgeUrl.orNull,
                mavenUrl = value.mavenUrl.orNull,
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

        tasks.upload.finalizedBy(releaseMetadataTask)
    }

    if (subprojects.isEmpty()) {
        createReleaseMetadata()
    }
}

private fun Project.createReleaseMetadata() {
    coreProject.tasks.releaseMetadata.releases.create(project.name) {
        tag.convention(project.metadataTagConvention())
        preRelease.convention(project.the<UploadExtension>().maven.isSnapshot)
    }

    tasks.withType<PublishToMavenRepository> {
        doLast {
            val metadataUrl = repository.url
                .resolve(publication.groupId.replace('.', '/') + "/")
                .resolve(publication.artifactId + "/")
                .resolve(publication.version + "/")
                .resolve("maven-metadata.xml")
                .toURL()

            if (metadataUrl.protocol == "file") return@doLast
            val metadata =
                fetchMavenMetadata(repository.url, publication.groupId, publication.artifactId, publication.version)

            project.modifyReleaseMetadata {
                metadata.versioning.snapshot
            }

            val connection = metadataUrl.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw RuntimeException("unable to maven metadata after publishing to ${repository.url}: $responseCode")
            }

            val body = connection.inputStream.bufferedReader().readText()

            println(body)
        }
    }

}

fun Project.modifyReleaseMetadata(block: ReleaseMetadata.() -> Unit = {}) {
    coreProject.tasks.releaseMetadata.releases
        .named(project.name, block)
}

private fun MavenPublication.downloadUrl(repository: URI): String {
    val realVersion = if (version.contains("-SNAPSHOT")) {
        val metadata = fetchMavenMetadata(repository, groupId, artifactId, version)
        val snapshot = metadata.versioning.snapshot ?: error("uploaded snapshot version metadata not found")
        val value = "-${snapshot.timestamp}-${snapshot.buildNumber}"
        metadata.versioning.snapshotVersions
            .map { it.value }
            .find { it.endsWith(value) }
            ?: error("invalid snapshot maven metadata")
    } else {
        version
    }
}
