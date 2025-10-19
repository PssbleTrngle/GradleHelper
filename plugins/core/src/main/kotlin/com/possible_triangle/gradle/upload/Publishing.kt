package com.possible_triangle.gradle.upload

import com.possible_triangle.gradle.ModExtension
import com.possible_triangle.gradle.env
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
    fun githubPackages(block: MavenArtifactRepository.() -> Unit = {})
    fun nexus(snapshot: Boolean = false, block: MavenArtifactRepository.() -> Unit = {})
    fun removePomDependencies()
    fun removePomDependencies(groupId: String, artifactId: String? = null, version: String? = null)
    fun disableDefaultModifications()
}

data class DependencyFilter(
    val groupId: String? = null,
    val artifactId: String? = null,
    val version: String? = null,
    val scope: String? = null
)

internal class ModMavenPublishingExtensionImpl(
    private val project: Project,
) :
    ModMavenPublishingExtension {
    override val artifactVersion: Property<String> = project.objects.property<String>().convention(project.mod.version)
    override val group: Property<String> = project.objects.property<String>().convention(project.mod.mavenGroup)
    override val name: Property<String> = project.objects.property<String>().convention(project.defaultArtifactName())

    private val parentExtension = project.the<PublishingExtension>()

    override val repositories: RepositoryHandler get() = parentExtension.repositories
    override fun repositories(configure: RepositoryHandler.() -> Unit) = parentExtension.repositories(configure)

    override fun githubPackages(block: MavenArtifactRepository.() -> Unit) =
        repositories.addGithubPackages(project, block)

    override fun nexus(snapshot: Boolean, block: MavenArtifactRepository.() -> Unit) {
        val type = if (snapshot) "snapshots" else "releases"
        val token = env["NEXUS_TOKEN"]
        val user = env["NEXUS_USER"]

        if (token != null && user != null) repositories.addNexus(type) {
            credentials {
                username = user
                password = token
            }

            block()
        }
    }

    var removeAllDependency = false
        private set

    var applyDefaultModifications = true
        private set

    val dependencyFilters = arrayListOf<DependencyFilter>()

    override fun removePomDependencies() {
        removeAllDependency = true
    }

    override fun disableDefaultModifications() {
        applyDefaultModifications = false
    }

    override fun removePomDependencies(groupId: String, artifactId: String?, version: String?) {
        dependencyFilters.add(DependencyFilter(groupId, artifactId, version))
    }

    internal fun setup() {
        group.orNull?.let { mavenGroup ->
            project.configure<PublishingExtension> {
                repositories {
                    mavenLocal()
                }

                publications {
                    create<MavenPublication>(PUBLICATION_NAME) {
                        groupId = mavenGroup
                        artifactId = this@ModMavenPublishingExtensionImpl.name.get()
                        version = artifactVersion.get()

                        if (project.detectKotlin()) {
                            from(project.components["kotlin"])
                        } else {
                            from(project.components["java"])
                        }

                        if (applyDefaultModifications) {
                            defaultPomModifications(project.mod)
                        }

                        if (removeAllDependency) {
                            removePomDependencies()
                        } else dependencyFilters.forEach {
                            removePomDependencies(it)
                        }
                    }
                }
            }
        }
    }
}

private const val PUBLICATION_NAME = "maven"

fun Project.modifyPublication(block: MavenPublication.() -> Unit) = afterEvaluate {
    extensions.findByType<PublishingExtension>()?.apply {
        publications {
            named<MavenPublication>(PUBLICATION_NAME) {
                block()
            }
        }
    }
}

internal fun MavenPublication.defaultPomModifications(mod: ModExtension) {
    removePomDependencies(DependencyFilter(scope = "runtime"))

    mod.repository.orNull?.let { repository ->
        pom.issueManagement {
            system = "github"
            url = "https://github.com/${repository}/issues"
        }

        pom.scm {
            url = "https://github.com/${repository}"
        }
    }

    mod.author.orNull?.let { author ->
        pom.developers {
            developer {
                name = author
            }
        }
    }
}

fun MavenPublication.removePomDependencies() {
    suppressAllPomMetadataWarnings()

    pom.withXml {
        val node = asNode()
        val list = node.get("dependencies") as NodeList
        list.forEach { node.remove(it as Node) }
    }
}

private fun Node.all(key: String) = (get(key) as List<Node>?) ?: emptyList()
private fun Node.first(key: String) = all(key).firstOrNull()

private fun matchesOrNull(filter: String?, node: Node?): Boolean {
    if (node == null || filter == null) return false
    val value = when (val it = node.value()) {
        is NodeList -> it.first()
        is String -> it
        else -> null
    }
    return value == filter
}

private fun Node.test(filter: DependencyFilter): Boolean {
    return matchesOrNull(filter.groupId, first("groupId"))
            || matchesOrNull(filter.artifactId, first("artifactId"))
            || matchesOrNull(filter.version, first("version"))
            || matchesOrNull(filter.scope, first("scope"))
}

fun MavenPublication.removePomDependencies(filter: DependencyFilter) {
    suppressAllPomMetadataWarnings()

    pom.withXml {
        val node = asNode().first("dependencies") ?: return@withXml
        val dependencies = node.all("dependency")
        dependencies.forEach {
            if (it.test(filter)) node.remove(it)
        }
    }
}