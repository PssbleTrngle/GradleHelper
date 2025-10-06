package com.possible_triangle.gradle.upload

import com.possible_triangle.gradle.env
import com.possible_triangle.gradle.features.detectKotlin
import com.possible_triangle.gradle.features.loaders.ModLoader
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.property
import com.possible_triangle.gradle.stringProperty
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property

interface DependencyBuilder {
    fun required(dependency: String)
    fun optional(dependency: String)
    fun embedded(dependency: String)
}

interface AbstractUploadExtension {
    val projectId: Property<String>
    val token: Property<String>
    val file: RegularFileProperty

    val minecraftVersions: ListProperty<String>
    val version: Property<String>
    val versionName: Property<String>
    val changelog: Property<String>
    val releaseType: Property<String>

    val modLoaders: ListProperty<ModLoader>

    val includeKotlinDependency: Property<Boolean>

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
    val modLoaders: Collection<ModLoader>,
    val requiredDependencies: Collection<String>,
    val optionalDependencies: Collection<String>,
    val embeddedDependencies: Collection<String>,
)

internal abstract class AbstractUploadExtensionImpl(private val project: Project, private val platform: String) :
    AbstractUploadExtension {
    protected abstract fun DependencyBuilder.requireKotlin(loader: ModLoader)

    private val tokenKey = "${platform.uppercase()}_TOKEN"
    private val projectIdKey = "${platform}_project_id"

    protected val requiredDependencies = hashSetOf<String>()
    protected val optionalDependencies = hashSetOf<String>()
    protected val embeddedDependencies = hashSetOf<String>()

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

    override val projectId = project.objects.property(project.stringProperty(projectIdKey))
    override val token = project.objects.property(env[tokenKey])
    override val file: RegularFileProperty = project.objects.fileProperty()

    override val minecraftVersions =
        project.objects.listProperty<String>().convention(project.mod.minecraftVersion.map(::setOf))
    override val modLoaders = project.objects.listProperty<ModLoader>()
    override val version = project.objects.property(project.mod.version)
    override val versionName = project.objects.property<String>()
    override val changelog = project.objects.property(env["CHANGELOG"])
    override val releaseType = project.objects.property(project.mod.releaseType.orElse("release"))

    override var includeKotlinDependency = project.objects.property(true)

    override fun dependencies(block: DependencyBuilder.() -> Unit) = dependencies.let(block)

    fun build(): GatheredUploadInfo {
        val loaders = modLoaders.getOrElse(emptyList())

        loaders.ifEmpty { throw IllegalStateException("No mod loader specified") }

        if (includeKotlinDependency.get() && project.detectKotlin()) {
            loaders.forEach {
                dependencies.requireKotlin(it)
            }
        }

        return GatheredUploadInfo(
            projectId = projectId.orNull
                ?: throw IllegalStateException("$platform project ID missing. Provide using $projectIdKey gradle property or set manually"),
            token = token.orNull
                ?: throw IllegalStateException("$platform token missing! Provide using environmental variable $tokenKey or set manually"),
            file = file,
            minecraftVersions = minecraftVersions.getOrElse(emptyList())
                .ifEmpty { throw IllegalStateException("No minecraft version specified") },
            version = version.orNull ?: throw IllegalStateException("No version specified"),
            versionName = versionName.orNull ?: "${loaders.joinToString(", ") { it.name.lowercase().capitalized() }} $version",
            changelog = changelog.orNull ?: throw IllegalStateException("No changelog specified"),
            releaseType = releaseType.get(),
            modLoaders = loaders,
            requiredDependencies = requiredDependencies,
            optionalDependencies = optionalDependencies,
            embeddedDependencies = embeddedDependencies,
        )
    }

}

internal val TaskContainer.publish
    get(): Task {
        return findByName("publish") ?: register("publish") {
            doLast {
                print("successfully published")
            }
        }.get()
    }