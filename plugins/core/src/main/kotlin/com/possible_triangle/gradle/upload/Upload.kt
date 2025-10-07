package com.possible_triangle.gradle.upload

import org.gradle.api.Project

interface UploadExtension {
    val modrinth: ModrinthExtension
    val curseforge: CurseForgeExtension
    val maven: ModMavenPublishingExtension

    fun maven(block: ModMavenPublishingExtension.() -> Unit)
    fun curseforge(block: CurseForgeExtension.() -> Unit)
    fun modrinth(block: ModrinthExtension.() -> Unit)
    fun forEach(block: AbstractUploadExtension.() -> Unit)
}

internal open class UploadExtensionImpl(project: Project) : UploadExtension {
    override val modrinth = ModrinthExtensionImpl(project)
    override val curseforge = CurseForgeExtensionImpl(project)
    override val maven = ModMavenPublishingExtensionImpl(project)

    override fun maven(block: ModMavenPublishingExtension.() -> Unit) = maven.block()

    override fun curseforge(block: CurseForgeExtension.() -> Unit) {
        curseforge.block()
    }

    override fun modrinth(block: ModrinthExtension.() -> Unit) {
        modrinth.block()
    }

    override fun forEach(block: AbstractUploadExtension.() -> Unit) {
        curseforge(block)
        modrinth(block)
    }

    fun setup() {
        modrinth.setup()
        curseforge.setup()
        maven.setup()
    }
}