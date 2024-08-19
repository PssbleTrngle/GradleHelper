package com.possible_triangle.gradle.features.publishing

import com.possible_triangle.gradle.features.loaders.ModLoader
import net.darkhax.curseforgegradle.Constants
import net.darkhax.curseforgegradle.CurseForgeGradlePlugin
import net.darkhax.curseforgegradle.TaskPublishCurseForge
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.register

interface CurseforgeExtension : UploadExtension

private class CurseForgeExtensionImpl(project: Project) : UploadExtensionImpl(project, "curseforge"), CurseforgeExtension {
    override fun DependencyBuilder.requireKotlin(loader: ModLoader) {
        when(loader) {
            ModLoader.FORGE, ModLoader.NEOFORGE -> required("kotlin-for-forge")
            ModLoader.FABRIC -> required("fabric-language-kotlin")
        }
    }
}

private fun String.loaderName(): String {
    return when(lowercase()) {
        "neoforge" -> "NeoForge"
        else -> capitalized()
    }
}

fun Project.enableCursegradle(block: CurseforgeExtension.() -> Unit) {
    apply<CurseForgeGradlePlugin>()

    val uploadInfo = CurseForgeExtensionImpl(this).apply(block).buildIfToken() ?: return run {
        logger.warn("No curseforge token set, cursegradle will not be configured")
    }

    tasks.register<TaskPublishCurseForge>("curseforge") {
        apiToken = uploadInfo.token

        upload(uploadInfo.projectId, uploadInfo.file).apply {
            changelogType = Constants.CHANGELOG_MARKDOWN
            changelog = uploadInfo.changelog
            releaseType = uploadInfo.releaseType
            uploadInfo.modLoaders.forEach { addModLoader(it.loaderName()) }
            uploadInfo.minecraftVersions.forEach { addGameVersion(it) }
            displayName = uploadInfo.versionName

            uploadInfo.requiredDependencies.forEach { addRelation(it, Constants.RELATION_REQUIRED) }
            uploadInfo.optionalDependencies.forEach { addRelation(it, Constants.RELATION_OPTIONAL) }
            uploadInfo.embeddedDependencies.forEach { addRelation(it, Constants.RELATION_EMBEDDED) }
        }
    }
}