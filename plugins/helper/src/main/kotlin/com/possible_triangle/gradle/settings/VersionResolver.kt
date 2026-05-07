package com.possible_triangle.gradle.settings

import groovy.util.Node
import groovy.util.NodeList
import groovy.xml.XmlParser
import org.gradle.plugin.use.PluginId
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI

enum class ResolutionStrategy {
    NONE,
    SNAPSHOT,
    WILDCARD,
    FETCH,
}

private inline fun <reified T> Node.first(path: String): T {
    val list = get(path) as NodeList
    return list.first() as T
}

private inline fun <reified T> Node.value(): T = value() as T

private fun Node.values(): List<Node> = value<NodeList>().map { it as Node }

private fun parseMetadataVersions(from: String): List<String> {
    val parser = XmlParser()

    val xml = parser.parseText(from)

    val versioning = xml.first<Node>("versioning")
    val versions = versioning.first<Node>("versions")

    return versions.values().map { it.value<NodeList>().first() as String }
}

internal fun metadataUri(plugin: PluginId): URI = URI("https://plugins.gradle.org/m2/${plugin.namespace}/${plugin.name}/maven-metadata.xml")

internal fun fetchMetadataRaw(plugin: PluginId): InputStream {
    val uri = metadataUri(plugin)
    val connection = uri.toURL().openConnection() as HttpURLConnection
    connection.requestMethod = "GET"

    val responseCode: Int = connection.responseCode

    if (responseCode != HttpURLConnection.HTTP_OK) {
        throw RuntimeException("unable to fetch gradle helper plugin version for ${plugin.name}: $responseCode")
    }

    return connection.getInputStream()
}

private fun fetchVersion(
    majorVersion: String,
    plugin: PluginId,
): String {
    val inputStream = fetchMetadataRaw(plugin)
    val response = InputStreamReader(inputStream).readText()
    val versions = parseMetadataVersions(response)

    return versions
        .sorted()
        .last { it.startsWith("$majorVersion.") }
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
