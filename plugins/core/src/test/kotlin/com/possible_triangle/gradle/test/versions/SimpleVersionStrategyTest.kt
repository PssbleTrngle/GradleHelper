package com.possible_triangle.gradle.test.versions

import com.possible_triangle.gradle.upload.SimpleVersionStrategy
import kotlin.test.Test
import kotlin.test.assertEquals

class SimpleVersionStrategyTest {
    private val strategy = SimpleVersionStrategy()

    @Test
    fun `simple version strategy`() {
        val input = TestModVersionProperties("1.0.0")

        assertEquals("1.0.0", strategy.modVersion(input))
        assertEquals("1.0.0", strategy.artifactVersion(input))
        assertEquals("1.0.0", strategy.metadataTag(input))
        assertEquals("1.0.0", strategy.uploadVersion(input))
        assertEquals("test_mod", strategy.artifactName(input))
        assertEquals("test_mod-1.0.0", strategy.baseName(input))
    }

    @Test
    fun `simple version strategy with suffix`() {
        val input = TestModVersionProperties("0.0.0-dev")

        assertEquals("0.0.0-dev", strategy.modVersion(input))
        assertEquals("0.0.0-dev", strategy.artifactVersion(input))
        assertEquals("0.0.0-dev", strategy.metadataTag(input))
        assertEquals("0.0.0-dev", strategy.uploadVersion(input))
        assertEquals("test_mod", strategy.artifactName(input))
        assertEquals("test_mod-0.0.0-dev", strategy.baseName(input))
    }

    @Test
    fun `simple version strategy with metadata`() {
        val input = TestModVersionProperties("1.2.3+loader")

        assertEquals("1.2.3", strategy.modVersion(input))
        assertEquals("1.2.3", strategy.artifactVersion(input))
        assertEquals("1.2.3+loader", strategy.metadataTag(input))
        assertEquals("1.2.3", strategy.uploadVersion(input))
        assertEquals("test_mod", strategy.artifactName(input))
        assertEquals("test_mod-1.2.3", strategy.baseName(input))
    }
}
