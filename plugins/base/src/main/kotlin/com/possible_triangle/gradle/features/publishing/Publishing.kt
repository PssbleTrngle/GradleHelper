package com.possible_triangle.gradle.features.publishing

import com.possible_triangle.gradle.features.detectKotlin
import com.possible_triangle.gradle.features.loaders.isSubProject
import com.possible_triangle.gradle.mod
import groovy.util.Node
import groovy.util.NodeList
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.internal.cc.base.logger
import org.gradle.kotlin.dsl.*
import java.net.URI

fun RepositoryHandler.addGithubPackages(project: Project, block: MavenArtifactRepository.() -> Unit) =
    addGithubPackages(project.mod.repository.get(), block)


fun RepositoryHandler.addGithubPackages(repository: String, block: MavenArtifactRepository.() -> Unit) {
    val actor = env["GITHUB_ACTOR"]
    val token = env["GITHUB_TOKEN"]

    if (actor == null || token == null) return

    maven {
        name = "GitHubPackages"
        url = URI("https://maven.pkg.github.com/${repository.lowercase()}")
        credentials {
            username = actor
            password = token
        }

        block()
    }
}

fun RepositoryHandler.addNexus(type: String, block: MavenArtifactRepository.() -> Unit) {
    maven {
        name = "Nexus"
        url = URI("https://registry.somethingcatchy.net/repository/maven-$type/")
        block()
    }
}

private fun Project.defaultArtifactName(): Provider<String> {
    return mod.id.map { modId ->
        if (isSubProject) "${modId}-${name.lowercase()}"
        else modId
    }
}

interface ModMavenPublishingExtension {
    val artifactVersion: Property<String>
    val group: Property<String>
    val name: Property<String>
    val repositories: RepositoryHandler
    fun repositories(configure: RepositoryHandler.() -> Unit)
    fun githubPackages()
    fun nexus(snapshot: Boolean = false)
    fun removePomDependencies()
    fun removePomDependencies(groupId: String, artifactId: String? = null, version: String? = null)
}

private data class DependencyFilter(val groupId: String, val artifactId: String?, val version: String?)

private class ModMavenPublishingExtensionImpl(
    private val project: Project,
    private val parentExtension: PublishingExtension,
) :
    ModMavenPublishingExtension {
    override val artifactVersion: Property<String> = project.objects.property<String>().convention(project.mod.version)
    override val group: Property<String> = project.objects.property<String>().convention(project.mod.mavenGroup)
    override val name: Property<String> = project.objects.property<String>().convention(project.defaultArtifactName())

    override val repositories: RepositoryHandler get() = parentExtension.repositories
    override fun repositories(configure: RepositoryHandler.() -> Unit) = parentExtension.repositories(configure)

    override fun githubPackages() = repositories.githubPackages(project)

    override fun nexus(snapshot: Boolean) {
        val type = if (snapshot) "snapshots" else "releases"
        val token = env["NEXUS_TOKEN"]
        val user = env["NEXUS_USER"]

        if (token != null && user != null) repositories.nexus(type) {
            credentials {
                username = user
                password = token
            }
        } else {
            logger.warn("skipping nexus publishing")
        }
    }

    var removeAllDependency = false
        private set

    val dependencyFilters = arrayListOf<DependencyFilter>()

    override fun removePomDependencies() {
        removeAllDependency = true
    }

    override fun removePomDependencies(groupId: String, artifactId: String?, version: String?) {
        dependencyFilters.add(DependencyFilter(groupId, artifactId, version))
    }
}

internal const val PUBLICATION_NAME = "maven"

fun Project.enableMavenPublishing(block: ModMavenPublishingExtension.() -> Unit) {
    apply<MavenPublishPlugin>()

    configure<PublishingExtension> {
        val config = ModMavenPublishingExtensionImpl(this@enableMavenPublishing, this).apply(block)

        repositories {
            mavenLocal()
        }

        publications {
            create<MavenPublication>(PUBLICATION_NAME) {
                groupId = config.group.get()
                artifactId = config.name.get()
                version = config.artifactVersion.get()

                if (project.detectKotlin()) {
                    from(components["kotlin"])
                } else {
                    from(components["java"])
                }

                if (config.removeAllDependency) {
                    removePomDependencies()
                } else config.dependencyFilters.forEach {
                    removePomDependencies(it)
                }
            }
        }
    }
}

fun Project.modifyPublication(block: MavenPublication.() -> Unit) {
    extensions.findByType<PublishingExtension>()?.apply {
        publications {
            named<MavenPublication>(PUBLICATION_NAME) {
                block()
            }
        }
    }
}

internal fun MavenPublication.removePomDependencies() {
    suppressAllPomMetadataWarnings()

    pom.withXml {
        val node = asNode()
        val list = node.get("dependencies") as NodeList
        list.forEach { node.remove(it as Node) }
    }
}

private fun MavenPublication.removePomDependencies(filter: DependencyFilter) {
    suppressAllPomMetadataWarnings()

    fun Node.all(key: String) = get(key) as List<Node>? ?: emptyList()
    fun Node.first(key: String) = all(key).firstOrNull()

    pom.withXml {
        val node = asNode().first("dependencies") ?: return@withXml
        val dependencies = node.all("dependency")
        dependencies
            .filter { it.first("groupId")?.value() == filter.groupId }
            .filter { filter.artifactId == null || (it.first("artifactId")?.value() == filter.artifactId) }
            .filter { filter.version == null || (it.first("version")?.value() == filter.version) }
            .forEach { node.remove(it) }
    }
}