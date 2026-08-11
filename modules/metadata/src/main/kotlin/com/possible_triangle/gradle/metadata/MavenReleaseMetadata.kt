package com.possible_triangle.gradle.metadata

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI

class HttpRequestException(val responseCode: Int) : RuntimeException("http request failed: $responseCode")

@Serializable
@XmlSerialName("metadata")
 data class MavenMetadata(
    @XmlElement val versioning: Versioning
)

@Serializable
@XmlSerialName("versioning")
 data class Versioning(
    @XmlElement val latest: String? = null,
    @XmlElement val release: String? = null,
    @XmlChildrenName("version") val versions: List<String> = emptyList(),
    @XmlElement val snapshot: Snapshot? = null,
    @XmlChildrenName("snapshotVersion") val snapshotVersions: List<SnapshotVersion> = emptyList(),
)

@Serializable
@XmlSerialName("snapshot")
 data class Snapshot(
    @XmlElement val timestamp: String,
    @XmlElement val buildNumber: String,
)

@Serializable
data class SnapshotVersion(
    @XmlElement val classifier: String? = null,
    @XmlElement val extension: String,
    @XmlElement val value: String,
)

private val Xml = XML.v1 {
    policy {
        ignoreUnknownChildren()
    }
}

internal fun parseMetadataVersions(from: String): MavenMetadata {
    return Xml.decodeFromString<MavenMetadata>(from)
}

internal fun metadataUri(repository: URI, groupId: String, artifactId: String, version: String?): URI =
    repository
        .resolve(groupId.replace('.', '/') + "/")
        .resolve("$artifactId/")
        .let {
            if (version == null) it
            it.resolve("$version/")
        }
        .resolve("maven-metadata.xml")


internal fun fetchMetadataRaw(repository: URI, groupId: String, artifactId: String, version: String?): InputStream {
    val uri = metadataUri(repository, groupId, artifactId, version)
    val connection = uri.toURL().openConnection() as HttpURLConnection
    connection.requestMethod = "GET"

    val responseCode: Int = connection.responseCode

    if (responseCode != HttpURLConnection.HTTP_OK) {
        throw HttpRequestException(responseCode)
    }

    return connection.getInputStream()
}

fun fetchMavenMetadata(
    repository: URI, groupId: String, artifactId: String, version: String? = null
): MavenMetadata {
    val inputStream = fetchMetadataRaw(repository, groupId, artifactId, version)
    val response = InputStreamReader(inputStream).readText()
   return parseMetadataVersions(response)
}
