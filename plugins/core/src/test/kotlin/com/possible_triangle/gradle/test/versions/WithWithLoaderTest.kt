package com.possible_triangle.gradle.test.versions

import com.possible_triangle.gradle.upload.SimpleVersionStrategy
import com.possible_triangle.gradle.upload.WithLoader
import kotlin.test.Test
import kotlin.test.assertEquals

class WithWithLoaderTest {
    private val strategy = WithLoader(SimpleVersionStrategy())

    @Test
    fun `simple version strategy`() {
        val input = TestModVersionProperties("1.0.0")

        assertEquals("1.0.0", strategy.modVersion(input))
        assertEquals("1.0.0", strategy.artifactVersion(input))
        assertEquals("1.0.0+loader", strategy.metadataTag(input))
        assertEquals("1.0.0", strategy.uploadVersion(input))
        assertEquals("test_mod-loader", strategy.artifactName(input))
        assertEquals("test_mod-loader-1.0.0", strategy.baseName(input))
    }

    @Test
    fun `simple version strategy with suffix`() {
        val input = TestModVersionProperties("0.0.0-dev")

        assertEquals("0.0.0-dev", strategy.modVersion(input))
        assertEquals("0.0.0-dev", strategy.artifactVersion(input))
        assertEquals("0.0.0-dev+loader", strategy.metadataTag(input))
        assertEquals("0.0.0-dev", strategy.uploadVersion(input))
        assertEquals("test_mod-loader", strategy.artifactName(input))
        assertEquals("test_mod-loader-0.0.0-dev", strategy.baseName(input))
    }

    @Test
    fun `simple version strategy different metadata`() {
        val input = TestModVersionProperties("1.2.3+mc1.21.1")

        assertEquals("1.2.3", strategy.modVersion(input))
        assertEquals("1.2.3", strategy.artifactVersion(input))
        assertEquals("1.2.3+mc1.21.1.loader", strategy.metadataTag(input))
        assertEquals("1.2.3", strategy.uploadVersion(input))
        assertEquals("test_mod-loader", strategy.artifactName(input))
        assertEquals("test_mod-loader-1.2.3", strategy.baseName(input))
    }
}
