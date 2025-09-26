package com.possible_triangle.gradle.packwiz

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
data class PackwizUpdateInfo(val modrinth: ModrinthUpdateInfo? = null)

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

fun DependencyResolutionManagement.importPackwiz(from: File, name: String = "pack") {
    if (!from.exists()) return

    val index = TOML.decodeFromString<PackwizIndex>(from.readText())
    val mods = index.files
        .filter { it.metafile }
        .map { from.parentFile.resolve(it.file) }
        .associate { it.name.substringBefore('.') to it.readText() }
        .mapValues { TOML.decodeFromString<PackwizFile>(it.value) }

    if (mods.isEmpty()) return

    versionCatalogs.create(name) {
        mods.forEach { (slug, it) ->
            if (it.update.modrinth != null) {
                library("modrinth-$slug", "maven.modrinth", it.update.modrinth.modId).version(it.update.modrinth.version)
            }
        }
    }
}