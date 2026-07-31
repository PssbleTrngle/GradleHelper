package com.possible_triangle.gradle.test.versions

import com.possible_triangle.gradle.ModVersionProperties
import org.gradle.api.internal.provider.DefaultProvider

internal class TestModVersionProperties(
    version: String,
    minecraftVersion: String = "1.21.1",
    id: String = "test_mod",
    loader: String = "loader",
) : ModVersionProperties {
    override val id = DefaultProvider { id }
    override val version = DefaultProvider { version }
    override val minecraftVersion = DefaultProvider { minecraftVersion }
    override val loader = DefaultProvider { loader }
}
