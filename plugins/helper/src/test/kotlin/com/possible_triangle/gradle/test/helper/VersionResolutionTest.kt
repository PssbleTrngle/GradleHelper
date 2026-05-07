package com.possible_triangle.gradle.test.helper

import com.possible_triangle.gradle.settings.ResolutionStrategy
import com.possible_triangle.gradle.settings.fetchMetadataRaw
import com.possible_triangle.gradle.settings.versionOf
import io.mockk.every
import io.mockk.mockkStatic
import org.gradle.plugin.use.internal.DefaultPluginId
import kotlin.test.Test
import kotlin.test.assertEquals

class VersionResolutionTest {
    @Test
    fun `extracts correct version from maven metadata`() {
        val plugin = DefaultPluginId.of("com.possible-triangle.core")

        mockkStatic(::fetchMetadataRaw)
        every { fetchMetadataRaw(plugin) } returns VersionResolutionTest::class.java.getResourceAsStream("/responses/core.xml")!!

        val version = ResolutionStrategy.FETCH.versionOf(plugin)

        assertEquals("99.0.150", version)
    }
}
