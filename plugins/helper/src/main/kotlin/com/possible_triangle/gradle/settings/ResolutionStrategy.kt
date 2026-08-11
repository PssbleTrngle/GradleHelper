package com.possible_triangle.gradle.settings

import com.possible_triangle.gradle.metadata.HttpRequestException
import com.possible_triangle.gradle.metadata.fetchMavenMetadata
import org.gradle.plugin.use.PluginId
import java.net.URI

enum class ResolutionStrategy {
    NONE,
    SNAPSHOT,
    WILDCARD,
    FETCH,
}

private val MAVEN_BASE_URL = URI("https://registry.somethingcatchy.net/repository/maven-releases/")

private fun fetchVersion(
    majorVersion: String,
    plugin: PluginId,
): String {
    try {
        val metadata =
            fetchMavenMetadata(
                MAVEN_BASE_URL,
                plugin.namespace!!,
                plugin.id,
            )

        return metadata.versioning.versions.last {
            it.startsWith("$majorVersion.")
        }
    } catch (ex: HttpRequestException) {
        throw RuntimeException("unable to fetch gradle helper plugin version for ${plugin.name}: ${ex.responseCode}")
    }
}

internal fun ResolutionStrategy.versionOf(
    majorVersion: String,
    plugin: PluginId,
): String =
    when (this) {
        ResolutionStrategy.NONE -> error("no resolution strategy selected")
        ResolutionStrategy.SNAPSHOT -> "$majorVersion-SNAPSHOT"
        ResolutionStrategy.WILDCARD -> "$majorVersion.+"
        ResolutionStrategy.FETCH -> fetchVersion(majorVersion, plugin)
    }
