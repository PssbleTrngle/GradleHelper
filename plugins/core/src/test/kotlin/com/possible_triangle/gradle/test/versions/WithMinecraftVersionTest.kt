package com.possible_triangle.gradle.test.versions

import com.possible_triangle.gradle.upload.SimpleVersionStrategy
import com.possible_triangle.gradle.upload.WithMinecraftVersion
import kotlin.test.Test
import kotlin.test.assertEquals

class WithMinecraftVersionTest {
    private val strategy = WithMinecraftVersion(SimpleVersionStrategy())

    @Test
    fun `simple version strategy`() {
        val input = TestModVersionProperties("1.0.0")

        assertEquals("1.0.0", strategy.modVersion(input))
        assertEquals("1.0.0", strategy.artifactVersion(input))
        assertEquals("1.0.0+mc1.21.1", strategy.metadataTag(input))
        assertEquals("1.0.0", strategy.uploadVersion(input))
        assertEquals("test_mod-1.21.1", strategy.artifactName(input))
        assertEquals("test_mod-1.0.0", strategy.baseName(input))
    }

    @Test
    fun `simple version strategy with suffix`() {
        val input = TestModVersionProperties("0.0.0-dev")

        assertEquals("0.0.0-dev", strategy.modVersion(input))
        assertEquals("0.0.0-dev", strategy.artifactVersion(input))
        assertEquals("0.0.0-dev+mc1.21.1", strategy.metadataTag(input))
        assertEquals("0.0.0-dev", strategy.uploadVersion(input))
        assertEquals("test_mod-1.21.1", strategy.artifactName(input))
        assertEquals("test_mod-0.0.0-dev", strategy.baseName(input))
    }

    @Test
    fun `simple version strategy different metadata`() {
        val input = TestModVersionProperties("1.2.3+loader")

        assertEquals("1.2.3", strategy.modVersion(input))
        assertEquals("1.2.3", strategy.artifactVersion(input))
        assertEquals("1.2.3+loader.mc1.21.1", strategy.metadataTag(input))
        assertEquals("1.2.3", strategy.uploadVersion(input))
        assertEquals("test_mod-1.21.1", strategy.artifactName(input))
        assertEquals("test_mod-1.2.3", strategy.baseName(input))
    }
}
