package com.possible_triangle.gradle.packwiz

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

enum class ErrorStrategy {
    WARN, FAIL, SKIP
}

interface PackwizExtension {
    val strategy: Property<ErrorStrategy>
    val curseforge: Property<Boolean>
    val modrinth: Property<Boolean>
    val from: RegularFileProperty
    val name: Property<String>
}