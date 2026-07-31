package com.possible_triangle.gradle.upload

import com.possible_triangle.gradle.ModVersionProperties
import com.possible_triangle.gradle.coreProject
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.modImpl
import org.gradle.api.Project
import org.gradle.api.provider.Provider

private fun Project.moduleSuffix(): String {
    if (this == coreProject) return ""
    return "-${project.name.lowercase()}"
}

interface VersionStrategy {
    fun modVersion(mod: ModVersionProperties): String = mod.version.get()

    fun metadataTag(mod: ModVersionProperties): String = modVersion(mod)

    fun artifactName(mod: ModVersionProperties): String = mod.id.get()

    fun artifactVersion(mod: ModVersionProperties): String = modVersion(mod)

    fun uploadVersion(mod: ModVersionProperties): String = modVersion(mod)

    fun baseName(mod: ModVersionProperties): String = "${artifactName(mod)}-${modVersion(mod)}"
}

fun String.toSnapshot(): String =
    if (contains('+')) {
        val version = substringBefore('+')
        val metadata = substringAfter('+')
        "${version.toSnapshot()}+$metadata"
    } else if (contains('-')) {
        "$this.SNAPSHOT"
    } else {
        "$this-SNAPSHOT"
    }

fun String.stripVersionMetadata() = substringBeforeLast('+')

fun String.addVersionMetadata(part: String): String =
    if (contains('+')) {
        val parts = substringAfterLast('+')
        if (parts.contains(part)) {
            this
        } else {
            plus(".$part")
        }
    } else {
        plus("+$part")
    }

class PassthroughVersionStrategy : VersionStrategy

open class SimpleVersionStrategy : VersionStrategy {
    override fun metadataTag(mod: ModVersionProperties): String = super.modVersion(mod)

    override fun modVersion(mod: ModVersionProperties): String = super.modVersion(mod).stripVersionMetadata()
}

class WithMinecraftVersion(
    private val inner: VersionStrategy,
) : VersionStrategy {
    override fun modVersion(mod: ModVersionProperties): String = inner.modVersion(mod)

    override fun metadataTag(mod: ModVersionProperties): String =
        inner.metadataTag(mod).addVersionMetadata("mc${mod.minecraftVersion.get()}")

    override fun artifactName(mod: ModVersionProperties): String = "${inner.artifactName(mod)}-${mod.minecraftVersion.get()}"

    override fun baseName(mod: ModVersionProperties) = inner.baseName(mod)
}

class WithLoader(
    private val inner: VersionStrategy,
) : VersionStrategy {
    private val ModVersionProperties.loaderName
        get() =
            loader.orNull ?: error("cannot only WithLoader strategy in a loader project")

    override fun modVersion(mod: ModVersionProperties): String = inner.modVersion(mod)

    override fun metadataTag(mod: ModVersionProperties): String = inner.metadataTag(mod).addVersionMetadata(mod.loaderName)

    override fun artifactName(mod: ModVersionProperties): String = "${inner.artifactName(mod)}-${mod.loaderName}"
}

internal fun parseVersionStrategy(id: String): VersionStrategy =
    when (id.lowercase()) {
        "simple" -> SimpleVersionStrategy()
        "suffix_minecraft_version" -> WithMinecraftVersion(SimpleVersionStrategy())
        "with_minecraft_version" -> WithMinecraftVersion(SimpleVersionStrategy())
        "with_loader" -> WithLoader(SimpleVersionStrategy())
        else -> error("unknown version strategy '$id'")
    }

internal fun Project.metadataTagConvention(): Provider<String> = mod.versionStrategy.map { it.metadataTag(modImpl) }

internal fun Project.artifactVersionConvention(): Provider<String> = mod.versionStrategy.map { it.artifactVersion(modImpl) }

internal fun Project.uploadVersionConvention(): Provider<String> = mod.versionStrategy.map { it.uploadVersion(modImpl) }

internal fun Project.artifactNameConvention(): Provider<String> =
    mod.versionStrategy.map {
        val suffix = moduleSuffix()
        it.artifactName(modImpl) + suffix
    }

internal fun Project.baseNameConvention(): Provider<String> =
    mod.versionStrategy.map {
        val suffix = moduleSuffix()
        it.baseName(modImpl) + suffix
    }
