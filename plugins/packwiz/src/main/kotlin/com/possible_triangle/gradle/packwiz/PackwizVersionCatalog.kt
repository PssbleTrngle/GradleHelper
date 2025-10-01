package com.possible_triangle.gradle.packwiz

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.StringFormat
import kotlinx.serialization.decodeFromString
import org.gradle.api.initialization.resolve.DependencyResolutionManagement
import java.io.File

private val TOML = Toml(
    TomlInputConfig(
        ignoreUnknownNames = true
    )
)

@Serializable
data class PackwizDownloadInfo(val url: String)

@Serializable
data class ModrinthUpdateInfo(
    @SerialName("mod-id") val modId: String,
    val version: String,
)

@Serializable
data class CurseforgeUpdateInfo(
    @SerialName("file-id") val fileId: Int,
    @SerialName("project-id") val projectId: Int,
)

@Serializable
data class PackwizUpdateInfo(
    val modrinth: ModrinthUpdateInfo? = null,
    val curseforge: CurseforgeUpdateInfo? = null,
)

@Serializable
data class PackwizFile(
    val name: String,
    val download: PackwizDownloadInfo,
    val update: PackwizUpdateInfo
)

@Serializable
data class PackwizFileReference(val file: String, val metafile: Boolean)

@Serializable
data class PackwizIndex(val files: List<PackwizFileReference>)

private fun ErrorStrategy.execute(message: String, ex: Exception? = null) {
    when (this) {
        ErrorStrategy.WARN -> LOGGER.warn(message, ex)
        ErrorStrategy.FAIL -> RuntimeException(message, ex)
        else -> {}
    }
}

private inline fun <reified T> StringFormat.decodeFromFile(file: File): T {
    return decodeFromString<T>(file.readText())
}

fun DependencyResolutionManagement.importPackwiz(settings: PackwizExtension) {
    val from = settings.from.get().asFile
    val name = settings.name.get()
    val strategy = settings.strategy.get()

    if (!from.exists()) return strategy.execute("directory $from does not exist")

    val indexFile = from.resolve("index.toml")
    if (!indexFile.exists()) return strategy.execute("index.toml in $from does not exist")

    val index = try {
        TOML.decodeFromFile<PackwizIndex>(indexFile)
    } catch (ex: Exception) {
        return strategy.execute("unable to decode index.toml", ex)
    }

    val mods = index.files
        .filter { it.metafile }
        .map { from.resolve(it.file) }
        .associateBy { it.name.substringBefore('.') }
        .mapValues { runCatching { TOML.decodeFromFile<PackwizFile>(it.value) } }

    val failed = mods.filterValues { it.isFailure }.mapValues { it.value.exceptionOrNull()!! }
    val successful = mods.filterValues { it.isSuccess }.mapValues { it.value.getOrThrow() }

    if (failed.isNotEmpty()) {
        val messages = listOf("${failed.size} mods metadata files could not be decoded:") +
                failed.map { "  ${it.key}: ${it.value.message}" }
        strategy.execute(messages.joinToString("\n"))
    }

    if (successful.isEmpty()) return strategy.execute("no packwiz mods found")

    versionCatalogs.create(name) {
        successful.forEach { (slug, file) ->
            file.update.modrinth?.let {
                library("modrinth-$slug", "maven.modrinth", it.modId).version(it.version)
            }

            file.update.curseforge?.let {
                library("curseforge-$slug", "curse.maven", "$slug-${it.projectId}").version(it.fileId.toString())
            }
        }
    }
}