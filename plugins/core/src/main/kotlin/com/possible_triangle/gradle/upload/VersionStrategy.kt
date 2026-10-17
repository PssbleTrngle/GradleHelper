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
    fun modVersion(mod: ModVersionProperties): String

    fun metadataTag(mod: ModVersionProperties): String

    fun artifactName(mod: ModVersionProperties): String

    fun artifactVersion(mod: ModVersionProperties): String

    fun uploadVersion(mod: ModVersionProperties): String

    fun baseName(mod: ModVersionProperties): String
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

sealed class PassthroughVersionStrategy : VersionStrategy {
    override fun modVersion(mod: ModVersionProperties): String = mod.version.get()

    override fun metadataTag(mod: ModVersionProperties): String = modVersion(mod)

    override fun artifactName(mod: ModVersionProperties): String = mod.id.get()

    override fun artifactVersion(mod: ModVersionProperties): String = modVersion(mod)

    override fun uploadVersion(mod: ModVersionProperties): String = modVersion(mod)

    override fun baseName(mod: ModVersionProperties): String = "${artifactName(mod)}-${modVersion(mod)}"
}

open class SimpleVersionStrategy : PassthroughVersionStrategy() {
    override fun metadataTag(mod: ModVersionProperties): String = super.modVersion(mod)

    override fun modVersion(mod: ModVersionProperties): String = super.modVersion(mod).stripVersionMetadata()
}

open class WrappedVersionStrategy(
    protected val inner: VersionStrategy,
) : VersionStrategy {
    override fun modVersion(mod: ModVersionProperties) = inner.modVersion(mod)

    override fun metadataTag(mod: ModVersionProperties) = inner.metadataTag(mod)

    override fun artifactName(mod: ModVersionProperties) = inner.artifactName(mod)

    override fun baseName(mod: ModVersionProperties) = inner.baseName(mod)

    override fun artifactVersion(mod: ModVersionProperties) = inner.artifactVersion(mod)

    override fun uploadVersion(mod: ModVersionProperties) = inner.uploadVersion(mod)
}

class WithMinecraftVersion(
    inner: VersionStrategy,
) : WrappedVersionStrategy(inner) {
    override fun modVersion(mod: ModVersionProperties): String = inner.modVersion(mod)

    override fun metadataTag(mod: ModVersionProperties): String =
        inner.metadataTag(mod).addVersionMetadata("mc${mod.minecraftVersion.get()}")

    override fun artifactName(mod: ModVersionProperties): String = "${inner.artifactName(mod)}-${mod.minecraftVersion.get()}"
}

class WithLoader(
    inner: VersionStrategy,
) : WrappedVersionStrategy(inner) {
    private val ModVersionProperties.loaderName
        get() =
            loader.orNull ?: error("cannot only WithLoader strategy in a loader project")

    override fun modVersion(mod: ModVersionProperties): String = inner.modVersion(mod)

    override fun metadataTag(mod: ModVersionProperties): String = inner.metadataTag(mod).addVersionMetadata(mod.loaderName)

    override fun artifactName(mod: ModVersionProperties): String = "${inner.artifactName(mod)}-${mod.loaderName}"
}

private fun parseSingleVersionStrategy(id: String): (VersionStrategy) -> VersionStrategy =
    when (id.trim().lowercase()) {
        "simple" -> { _ -> SimpleVersionStrategy() }
        "with_minecraft_version", "suffix_minecraft_version" -> ::WithMinecraftVersion
        "with_loader" -> ::WithLoader
        else -> error("unknown version strategy '$id'")
    }

internal fun parseVersionStrategy(id: String): VersionStrategy {
    val ids = id.split(",")
    val factories = ids.map(::parseSingleVersionStrategy)
    return factories.fold(SimpleVersionStrategy() as VersionStrategy) { inner, factory -> factory(inner) }
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
