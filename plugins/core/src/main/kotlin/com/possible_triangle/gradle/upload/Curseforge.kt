package com.possible_triangle.gradle.upload

import com.possible_triangle.gradle.features.loaders.ModLoader
import net.darkhax.curseforgegradle.Constants
import net.darkhax.curseforgegradle.TaskPublishCurseForge
import org.gradle.api.Project
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.register

interface CurseForgeExtension : AbstractUploadExtension

private fun ModLoader.loaderName(): String {
    return when (this) {
        ModLoader.NEOFORGE -> "NeoForge"
        else -> name.lowercase().capitalized()
    }
}

internal class CurseForgeExtensionImpl(private val project: Project) :
    AbstractUploadExtensionImpl(project, "curseforge"),
    CurseForgeExtension {
    override fun DependencyBuilder.requireKotlin(loader: ModLoader) {
        when (loader) {
            ModLoader.FORGE, ModLoader.NEOFORGE -> required("kotlin-for-forge")
            ModLoader.FABRIC -> required("fabric-language-kotlin")
        }
    }

    override fun onSetup() {
        val task = project.tasks.register<TaskPublishCurseForge>("curseforge") {
            onlyIf { token.isPresent }

            apiToken = token.orNull

            upload(projectId.get(), file.get()).apply {
                changelogType = Constants.CHANGELOG_MARKDOWN
                changelog = this@CurseForgeExtensionImpl.changelog.get()
                releaseType = this@CurseForgeExtensionImpl.releaseType.get()
                modLoaders.get().forEach { addModLoader(it.loaderName()) }
                minecraftVersions.get().forEach { addGameVersion(it) }
                displayName = versionName.get()

                requiredDependencies.forEach { addRelation(it, Constants.RELATION_REQUIRED) }
                optionalDependencies.forEach { addRelation(it, Constants.RELATION_OPTIONAL) }
                embeddedDependencies.forEach { addRelation(it, Constants.RELATION_EMBEDDED) }
            }
        }

        project.tasks.publish.dependsOn(task)
    }
}