package com.possible_triangle.gradle.packwiz

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

enum class ErrorStrategy {
    WARN, FAIL, SKIP
}

interface PackwizExtension {
    val strategy: Property<ErrorStrategy>
    val packs: NamedDomainObjectContainer<PackwizConfiguration>
}

interface PackwizConfiguration {
    val name: String
    val from: RegularFileProperty
    val curseforge: Property<Boolean>
    val modrinth: Property<Boolean>
    val strategy: Property<ErrorStrategy>
}

const val DEFAULT_PACK_NAME = "default"