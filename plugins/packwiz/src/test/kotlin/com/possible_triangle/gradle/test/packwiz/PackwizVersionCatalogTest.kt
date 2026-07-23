package com.possible_triangle.gradle.test.packwiz

import com.possible_triangle.gradle.packwiz.*
import io.mockk.mockk
import org.apache.log4j.Logger
import org.gradle.api.file.RegularFile
import org.gradle.api.internal.provider.DefaultProvider
import org.junit.Test
import java.io.File
import kotlin.test.assertContains
import kotlin.test.assertEquals

class PackwizVersionCatalogTest {
    @Test
    fun `can parse example pack`() {
        val extension =
            object : PackwizExtensionInterface {
                override val strategy = DefaultProvider { ErrorStrategy.FAIL }
                override val verbose = DefaultProvider { false }
                override val packs = emptyList<PackwizConfiguration>()
            }

        val logger = mockk<Logger>()
        val catalog = PackwizVersionCatalog(extension, logger)

        val config =
            object : PackwizConfigurationInterface {
                override val name = "test"
                override val from = DefaultProvider { RegularFile { File(("src/test/resources/packs/example")) } }
                override val curseforge = DefaultProvider { true }
                override val modrinth = DefaultProvider { true }
                override val strategy = DefaultProvider { ErrorStrategy.FAIL }
            }

        val mods = catalog.findMods(config)
        assertEquals(5, mods.size)
        assertContains(mods, "creeperconfettiplus")
        assertContains(mods, "jei")
        assertContains(mods, "moonlight")
        assertContains(mods, "supplementaries")
        assertContains(mods, "supplementaries-squared")
    }
}
