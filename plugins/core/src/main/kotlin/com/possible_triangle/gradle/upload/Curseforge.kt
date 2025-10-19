package com.possible_triangle.gradle.upload

import com.possible_triangle.gradle.features.loaders.ModLoader
import net.darkhax.curseforgegradle.Constants
import net.darkhax.curseforgegradle.TaskPublishCurseForge
import net.darkhax.curseforgegradle.UploadArtifact
import org.gradle.api.Project
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.register

data class CurseForgeDependency(val slug: String, val id: Int? = null)

class CurseForgeDependencies : AbstractDependencyBuilder<CurseForgeDependency>() {
    override fun resolve(dependency: String) = CurseForgeDependency(dependency)

    fun required(dependency: String, id: Int) = required(CurseForgeDependency(dependency, id))
    fun optional(dependency: String, id: Int) = optional(CurseForgeDependency(dependency, id))
    fun embedded(dependency: String, id: Int) = embedded(CurseForgeDependency(dependency, id))
}

interface CurseForgeExtension : AbstractUploadExtension<CurseForgeDependencies>

private fun ModLoader.loaderName(): String {
    return when (this) {
        ModLoader.NEOFORGE -> "NeoForge"
        else -> name.lowercase().capitalized()
    }
}

private fun UploadArtifact.addDependencies(dependencies: Collection<CurseForgeDependency>, type: String) {
    dependencies.forEach {
        if (it.id != null) addRelation(it, type, it.id.toString())
        else addRelation(it, type)
    }
}

internal class CurseForgeExtensionImpl(private val project: Project) :
    AbstractUploadExtensionImpl<CurseForgeDependencies>(project, "curseforge"),
    CurseForgeExtension {

    override fun DependencyBuilder.requireKotlin(loader: ModLoader) {
        when (loader) {
            ModLoader.FORGE, ModLoader.NEOFORGE -> required("kotlin-for-forge")
            ModLoader.FABRIC -> required("fabric-language-kotlin")
        }
    }

    override val dependencies = CurseForgeDependencies()

    override fun onSetup() {
        if (!token.isPresent) return

        val task = project.tasks.register<TaskPublishCurseForge>("curseforge") {
            apiToken = token.get()

            upload(projectId.get(), file.get()).apply {
                changelogType = Constants.CHANGELOG_MARKDOWN
                changelog = this@CurseForgeExtensionImpl.changelog.get()
                releaseType = this@CurseForgeExtensionImpl.releaseType.get()
                modLoaders.get().forEach { addModLoader(it.loaderName()) }
                minecraftVersions.get().forEach { addGameVersion(it) }
                displayName = versionName.get()

                addDependencies(dependencies.required, Constants.RELATION_REQUIRED)
                addDependencies(dependencies.optional, Constants.RELATION_OPTIONAL)
                addDependencies(dependencies.embedded, Constants.RELATION_EMBEDDED)
            }
        }

        project.tasks.publish.dependsOn(task)
    }
}