package com.possible_triangle.gradle.features.publishing

import com.possible_triangle.gradle.features.detectKotlin
import com.possible_triangle.gradle.features.loaders.ModLoader
import com.possible_triangle.gradle.features.loaders.detectModLoader
import com.possible_triangle.gradle.stringProperty
import net.fabricmc.loom.task.RemapJarTask
import net.minecraftforge.gradle.userdev.UserDevPlugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.env
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.mod

interface DependencyBuilder {
    fun required(dependency: String)
    fun optional(dependency: String)
    fun embedded(dependency: String)
}

interface UploadExtension {
    var projectId: String?
    var token: String?
    val file: RegularFileProperty

    var minecraftVersions: Collection<String>
    var version: String?
    var versionName: String?
    var changelog: String?
    var releaseType: String

    var modLoaders: Collection<String>

    var includeKotlinDependency: Boolean

    fun dependencies(block: DependencyBuilder.() -> Unit)
}

data class GatheredUploadInfo(
    val projectId: String,
    val token: String,
    val file: Provider<RegularFile>,
    val minecraftVersions: Collection<String>,
    val version: String,
    val versionName: String,
    val changelog: String,
    val releaseType: String,
    val modLoaders: Collection<String>,
    val requiredDependencies: Collection<String>,
    val optionalDependencies: Collection<String>,
    val embeddedDependencies: Collection<String>,
)

internal abstract class UploadExtensionImpl(private val project: Project, private val platform: String) :
    UploadExtension {
    protected abstract fun DependencyBuilder.requireKotlin(loader: ModLoader)

    private val tokenKey = "${platform.uppercase()}_TOKEN"
    private val projectIdKey = "${platform}_project_id"

    private val requiredDependencies = hashSetOf<String>()
    private val optionalDependencies = hashSetOf<String>()
    private val embeddedDependencies = hashSetOf<String>()

    private val dependencies = object : DependencyBuilder {
        override fun required(dependency: String) {
            requiredDependencies.add(dependency)
        }

        override fun optional(dependency: String) {
            optionalDependencies.add(dependency)
        }

        override fun embedded(dependency: String) {
            embeddedDependencies.add(dependency)
        }
    }

    override var projectId: String? = project.stringProperty(projectIdKey)
    override var token: String? = project.env[tokenKey]
    override val file: RegularFileProperty = project.detectOutputJar()

    override var minecraftVersions: Collection<String> = setOfNotNull(project.mod.minecraftVersion.orNull)
    override var modLoaders: Collection<String> = emptySet()
    override var version: String? = project.mod.version.orNull
    override var versionName: String? = null
    override var changelog: String? = project.env["CHANGELOG"]
    override var releaseType: String = project.mod.releaseType.orNull ?: "release"

    override var includeKotlinDependency: Boolean = true

    override fun dependencies(block: DependencyBuilder.() -> Unit) = dependencies.let(block)

    fun buildIfToken() = token?.let { build() }

    fun build(): GatheredUploadInfo {
        val loader = project.detectModLoader()

        if (loader != null) {
            if (includeKotlinDependency && project.detectKotlin()) {
                dependencies.requireKotlin(loader)
            }
        }

        val loaders = modLoaders
            .ifEmpty { setOfNotNull(loader?.name?.lowercase()) }
            .ifEmpty { throw IllegalStateException("No mod loader specified") }

        return GatheredUploadInfo(
            projectId = projectId
                ?: throw IllegalStateException("$platform project ID missing. Provide using $projectIdKey gradle property or set manually"),
            token = token
                ?: throw IllegalStateException("$platform token missing! Provide using environmental variable $tokenKey or set manually"),
            file = file,
            minecraftVersions = minecraftVersions.ifEmpty { throw IllegalStateException("No minecraft version specified") },
            version = version ?: throw IllegalStateException("No version specified"),
            versionName = versionName ?: "${loaders.joinToString(", ") { it.capitalized() }} $version",
            changelog = changelog ?: throw IllegalStateException("No changelog specified"),
            releaseType = releaseType,
            modLoaders = loaders,
            requiredDependencies = requiredDependencies,
            optionalDependencies = optionalDependencies,
            embeddedDependencies = embeddedDependencies,
        )
    }

}

internal fun Project.detectOutputJar(): RegularFileProperty = objects.fileProperty().convention {
    val jarTask = tasks.getByName<Jar>("jar")
    val jarJarTask = tasks.findByPath(UserDevPlugin.JAR_JAR_TASK_NAME) as Jar?
    when (detectModLoader()) {
        ModLoader.FORGE, ModLoader.NEOFORGE -> jarJarTask?.takeIf { it.enabled } ?: jarTask
        ModLoader.FABRIC -> tasks.getByName<RemapJarTask>("remapJar")
        else -> jarTask
    }.archiveFile.get().asFile
}
