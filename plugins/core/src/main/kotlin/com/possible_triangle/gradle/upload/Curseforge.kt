package com.possible_triangle.gradle.upload

import com.possible_triangle.gradle.features.loaders.displayName
import com.possible_triangle.gradle.property
import com.possible_triangle.gradle.stringProperty
import net.darkhax.curseforgegradle.Constants
import net.darkhax.curseforgegradle.TaskPublishCurseForge
import net.darkhax.curseforgegradle.UploadArtifact
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.register

data class CurseForgeDependency(
    val slug: String,
    val id: Int? = null,
)

class CurseForgeDependencies : AbstractDependencyBuilder<CurseForgeDependency>() {
    override fun resolve(dependency: String) = CurseForgeDependency(dependency)

    fun required(
        dependency: String,
        id: Int,
    ) = required(CurseForgeDependency(dependency, id))

    fun optional(
        dependency: String,
        id: Int,
    ) = optional(CurseForgeDependency(dependency, id))

    fun embedded(
        dependency: String,
        id: Int,
    ) = embedded(CurseForgeDependency(dependency, id))
}

interface CurseForgeExtension : AbstractUploadExtension<CurseForgeDependencies> {
    val environment: Property<String>
}

private fun UploadArtifact.addDependency(
    it: CurseForgeDependency,
    type: String,
) {
    if (it.id != null) {
        addRelation(it.slug, type, it.id.toString())
    } else {
        addRelation(it.slug, type)
    }
}

internal class CurseForgeExtensionImpl(
    private val project: Project,
) : AbstractUploadExtensionImpl<CurseForgeDependencies>(project, "curseforge"),
    CurseForgeExtension {
    override val dependencies = CurseForgeDependencies()

    override val environment = project.objects.property(project.stringProperty("mod_environment"))

    override fun setup() {
        if (!isConfigured()) return

        val uploadTask =
            project.tasks.register<TaskPublishCurseForge>("curseforge") {
                apiToken = token.get()

                upload(projectId.get(), file.get()).apply {
                    changelogType = Constants.CHANGELOG_MARKDOWN
                    changelog = this@CurseForgeExtensionImpl.changelog.orNull
                        ?: error("no changelog provided, unable to upload to curseforge")
                    releaseType = this@CurseForgeExtensionImpl.releaseType.get()
                    modLoaders.get().forEach { addModLoader(it.displayName()) }
                    minecraftVersions.get().forEach { addGameVersion(it) }
                    displayName = versionName.get()

                    environment.orNull?.let {
                        val value = it.lowercase().capitalized()
                        if (value != "Client" && value != "Server") {
                            error("$value is not a valid environment type")
                        }
                        addGameVersion(value)
                    }

                    dependencies.consume(
                        DependencyConsumer(
                            required = { addDependency(it, Constants.RELATION_REQUIRED) },
                            optional = { addDependency(it, Constants.RELATION_OPTIONAL) },
                            embedded = { addDependency(it, Constants.RELATION_EMBEDDED) },
                        ),
                    )
                }
            }

        project.addUploadTask("curseforge", uploadTask.get())

        // TODO would require project slug, because links on curseforge do not work with the project ID
        // project.modifyReleaseMetadata {
        //     curseforgeUrl.set(project.provider {
        //         val artifact = uploadTask.get().uploadArtifacts.firstOrNull()
        //         artifact?.let { "https://www.curseforge.com/minecraft/mc-mods/${it.slug}/files/${it.curseFileId}" }
        //     })
        // }
    }
}
