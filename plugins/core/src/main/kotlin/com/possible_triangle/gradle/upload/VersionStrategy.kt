package com.possible_triangle.gradle.upload

import com.possible_triangle.gradle.ModExtension
import com.possible_triangle.gradle.coreProject
import com.possible_triangle.gradle.mod
import org.gradle.api.Project
import org.gradle.api.provider.Provider

private fun Project.moduleSuffix(): String {
    if (this == coreProject) return ""
    return "-${project.name.lowercase()}"
}

interface VersionStrategy {
    fun modVersion(mod: ModExtension): String

    fun metadataTag(mod: ModExtension): String = modVersion(mod)

    fun artifactName(mod: ModExtension): String
}

open class SimpleVersionStrategy : VersionStrategy {
    override fun modVersion(mod: ModExtension) = mod.version.get()

    override fun artifactName(mod: ModExtension): String = mod.id.get()
}

class WithMinecraftSuffixStrategy(
    private val inner: VersionStrategy,
) : VersionStrategy {
    override fun modVersion(mod: ModExtension): String = inner.modVersion(mod)

    override fun metadataTag(mod: ModExtension): String = "${inner.metadataTag(mod)}+${mod.minecraftVersion.get()}"

    override fun artifactName(mod: ModExtension): String = "${inner.artifactName(mod)}-${mod.minecraftVersion.get()}"
}

internal fun parseVersionStrategy(id: String): VersionStrategy =
    when (id.lowercase()) {
        "simple" -> SimpleVersionStrategy()
        "suffix_minecraft_version" -> WithMinecraftSuffixStrategy(SimpleVersionStrategy())
        else -> error("unknown version strategy '$id'")
    }

internal fun Project.metadataTagConvention(): Provider<String> = mod.versionStrategy.map { it.metadataTag(mod) }

internal fun Project.artifactNameConvention(): Provider<String> =
    mod.versionStrategy.map {
        val suffix = moduleSuffix()
        it.artifactName(mod) + suffix
    }
