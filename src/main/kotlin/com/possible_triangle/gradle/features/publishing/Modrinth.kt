package com.possible_triangle.gradle.features.publishing

import com.modrinth.minotaur.Minotaur
import com.modrinth.minotaur.ModrinthExtension
import com.possible_triangle.gradle.features.loaders.ModLoader
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

private class ModrinthExtensionImpl(project: Project) : UploadExtensionImpl(project) {
    override val platform = "modrinth"

    override fun DependencyBuilder.requireKotlin(loader: ModLoader) {
        when(loader) {
            ModLoader.FORGE -> required("ordsPcFz")
            ModLoader.FABRIC -> required("Ha28R6CL")
        }
    }
}

fun Project.enableMinotaur(block: UploadExtension.() -> Unit) {
    apply<Minotaur>()

    val uploadInfo = ModrinthExtensionImpl(this).apply(block).buildIfToken() ?: return run {
        logger.warn("No modrinth token set, minotaur will not be configured")
    }

    configure<ModrinthExtension> {
        token.set(uploadInfo.token)
        projectId.set(uploadInfo.projectId)
        versionNumber.set(uploadInfo.version)
        versionName.set(uploadInfo.versionName)
        changelog.set(uploadInfo.changelog)
        gameVersions.set(uploadInfo.minecraftVersions)
        loaders.set(uploadInfo.modLoaders)
        versionType.set(uploadInfo.releaseType)
        uploadFile.set(uploadInfo.file)

        uploadInfo.requiredDependencies.forEach { required.project(it) }
        uploadInfo.optionalDependencies.forEach { optional.project(it) }
        uploadInfo.embeddedDependencies.forEach { embedded.project(it) }

        /* TODO
        syncBodyFrom.set(project.file("README.md").readText())

        tasks.named("modrinth") {
            dependsOn(tasks.withType<TaskModrinthSyncBody>())
        }
        */
    }
}