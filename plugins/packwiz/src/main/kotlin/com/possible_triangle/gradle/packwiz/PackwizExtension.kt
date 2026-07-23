package com.possible_triangle.gradle.packwiz

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

enum class ErrorStrategy {
    WARN,
    FAIL,
    SKIP,
}

interface PackwizExtensionInterface {
    val strategy: Provider<ErrorStrategy>
    val verbose: Provider<Boolean>
    val packs: Collection<PackwizConfiguration>
}

interface PackwizExtension : PackwizExtensionInterface {
    override val strategy: Property<ErrorStrategy>
    override val verbose: Property<Boolean>
    override val packs: NamedDomainObjectContainer<PackwizConfiguration>
}

interface PackwizConfigurationInterface {
    val name: String
    val from: Provider<RegularFile>
    val curseforge: Provider<Boolean>
    val modrinth: Provider<Boolean>
    val strategy: Provider<ErrorStrategy>
}

interface PackwizConfiguration : PackwizConfigurationInterface {
    override val name: String
    override val from: RegularFileProperty
    override val curseforge: Property<Boolean>
    override val modrinth: Property<Boolean>
    override val strategy: Property<ErrorStrategy>
}

const val DEFAULT_PACK_NAME = "default"
