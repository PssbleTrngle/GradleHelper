package com.possible_triangle.gradle.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.apache.log4j.LogManager
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI

@Serializable
private data class Response(
    val versions: List<String>
)

object NeoformFetcher {

    private const val API_ENDPOINT = "https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/neoform"

    private val logger = LogManager.getLogger(NeoformFetcher::class.java)

    private val JSON = Json {
        ignoreUnknownKeys = true
    }

    private fun fetch(): Response {
        val url = URI(API_ENDPOINT).toURL()
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "GET"
        val responseCode: Int = connection.responseCode

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw RuntimeException("unable to fetch neoform versions: $responseCode")
        }

        val response = InputStreamReader(connection.inputStream).readText()
        return JSON.decodeFromString<Response>(response)
    }

    fun fetchFor(minecraftVersion: String): String {
        val versions = fetch().versions
        val match = versions.findLast { it.startsWith("$minecraftVersion-") }
        if (match == null) throw RuntimeException("Could not find neoform version for minecraft $minecraftVersion")
        logger.info("Using NeoForm version $match")
        return match
    }

}