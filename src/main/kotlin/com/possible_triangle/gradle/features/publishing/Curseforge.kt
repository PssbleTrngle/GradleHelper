package com.possible_triangle.gradle.features.publishing

import com.possible_triangle.gradle.features.loaders.ModLoader
import net.darkhax.curseforgegradle.Constants
import net.darkhax.curseforgegradle.CurseForgeGradlePlugin
import net.darkhax.curseforgegradle.TaskPublishCurseForge
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.register

private class CurseForgeExtensionImpl(project: Project) : UploadExtensionImpl(project, "curseforge") {
    override fun DependencyBuilder.requireKotlin(loader: ModLoader) {
        when(loader) {
            ModLoader.FORGE -> required("kotlin-for-forge")
            ModLoader.FABRIC -> required("fabric-language-kotlin")
        }
    }
}

fun Project.enableCursegradle(block: UploadExtension.() -> Unit) {
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
            uploadInfo.modLoaders.forEach { addModLoader(it.capitalized()) }
            uploadInfo.minecraftVersions.forEach { addGameVersion(it) }
            displayName = uploadInfo.versionName

            uploadInfo.requiredDependencies.forEach { addRelation(it, Constants.RELATION_REQUIRED) }
            uploadInfo.optionalDependencies.forEach { addRelation(it, Constants.RELATION_OPTIONAL) }
            uploadInfo.embeddedDependencies.forEach { addRelation(it, Constants.RELATION_EMBEDDED) }
        }
    }
}