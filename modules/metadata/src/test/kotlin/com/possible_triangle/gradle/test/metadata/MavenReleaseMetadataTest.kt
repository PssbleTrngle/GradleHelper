package com.possible_triangle.gradle.test.metadata

import com.possible_triangle.gradle.metadata.parseMetadataVersions
import kotlin.test.Test

class MavenReleaseMetadataTest {

    private fun readResponse(type: String): String {
        val stream = MavenReleaseMetadataTest::class.java.getResourceAsStream("/responses/maven-metadata-${type}.xml")!!
        return stream.bufferedReader().readText()
    }

    @Test
    fun `resolves all package artifact versions`() {
        val response = readResponse("artifact")
        val actual = parseMetadataVersions(response)
        val expected = ""
    }

    @Test
    fun `resolves snapshot versions`() {
        val response = readResponse("snapshot")
        val actual = parseMetadataVersions(response)
        val expected = ""
    }

}
