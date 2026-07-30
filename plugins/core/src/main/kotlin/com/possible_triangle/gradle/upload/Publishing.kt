package com.possible_triangle.gradle.upload

import com.possible_triangle.gradle.env
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.property
import com.possible_triangle.gradle.publishing.DependencyFilter
import com.possible_triangle.gradle.publishing.removeDependencies
import com.possible_triangle.gradle.publishing.removeRuntimeDependencies
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.*
import java.net.URI

fun RepositoryHandler.addGithubPackages(
    project: Project,
    block: MavenArtifactRepository.() -> Unit,
) = addGithubPackages(project.mod.repository.get(), block)

fun RepositoryHandler.addGithubPackages(
    repository: String,
    block: MavenArtifactRepository.() -> Unit,
) {
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

fun RepositoryHandler.addNexus(
    type: String,
    block: MavenArtifactRepository.() -> Unit,
) {
    maven {
        name = "Nexus"
        setUrl("https://registry.somethingcatchy.net/repository/maven-$type/")
        block()
    }
}

fun RepositoryHandler.addNexus(
    type: Provider<String>,
    block: MavenArtifactRepository.() -> Unit,
) {
    maven {
        name = "Nexus"
        setUrl(type.map { "https://registry.somethingcatchy.net/repository/maven-$it/" })
        block()
    }
}

interface ModMavenPublishingExtension {
    val isSnapshot: Property<Boolean>
    val artifactVersion: Property<String>
    val group: Property<String>
    val name: Property<String>
    val repositories: RepositoryHandler

    fun repositories(configure: RepositoryHandler.() -> Unit)

    fun githubPackages(block: MavenArtifactRepository.() -> Unit = {})

    fun nexus(block: MavenArtifactRepository.() -> Unit = {})

    fun nexus(
        snapshot: Boolean,
        block: MavenArtifactRepository.() -> Unit = {},
    )

    fun removePomDependencies()

    fun removePomDependencies(
        groupId: String,
        artifactId: String? = null,
        version: String? = null,
    )

    fun disableDefaultModifications()
}

private const val PUBLICATION_NAME = "maven"

internal class ModMavenPublishingExtensionImpl(
    private val project: Project,
) : ModMavenPublishingExtension {
    override val isSnapshot: Property<Boolean> = project.objects.property(env["SNAPSHOT"] == "true")
    override val artifactVersion: Property<String> =
        project.objects.property(project.mod.version)
    override val group: Property<String> = project.objects.property(project.mod.mavenGroup)
    override val name: Property<String> =
        project.objects.property(project.artifactNameConvention())

    private val parentExtension get() = project.the<PublishingExtension>()

    override val repositories: RepositoryHandler get() = parentExtension.repositories

    override fun repositories(configure: RepositoryHandler.() -> Unit) = parentExtension.repositories(configure)

    override fun githubPackages(block: MavenArtifactRepository.() -> Unit) = repositories.addGithubPackages(project, block)

    override fun nexus(block: MavenArtifactRepository.() -> Unit) {
        nexus(isSnapshot.get(), block)
    }

    override fun nexus(
        snapshot: Boolean,
        block: MavenArtifactRepository.() -> Unit,
    ) {
        nexus(project.provider { snapshot }, block)
    }

    private fun nexus(
        snapshot: Provider<Boolean>,
        block: MavenArtifactRepository.() -> Unit,
    ) {
        val type = snapshot.map { if (it) "snapshots" else "releases" }
        val token = env["NEXUS_TOKEN"]
        val user = env["NEXUS_USER"]

        if (token != null && user != null) {
            repositories.addNexus(type) {
                credentials {
                    username = user
                    password = token
                }

                block()
            }
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

    override fun removePomDependencies(
        groupId: String,
        artifactId: String?,
        version: String?,
    ) {
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
                        version =
                            artifactVersion.get().let {
                                if (isSnapshot.get()) {
                                    "$it-SNAPSHOT"
                                } else {
                                    it
                                }
                            }

                        from(project.components["java"])

                        if (applyDefaultModifications) {
                            defaultPomModifications(project)
                        }

                        if (removeAllDependency) {
                            project.removeDependencies(this)
                        } else {
                            dependencyFilters.forEach {
                                project.removeDependencies(this, it)
                            }
                        }
                    }
                }
            }

            project.addUploadTask("publish", project.tasks.getByName("publish"))
        }
    }
}

fun Project.modifyPublication(block: MavenPublication.() -> Unit) =
    afterEvaluate {
        extensions.findByType<PublishingExtension>()?.apply {
            publications {
                val publication = findByName(PUBLICATION_NAME) as MavenPublication?
                publication?.block()
            }
        }
    }

internal fun MavenPublication.defaultPomModifications(project: Project) {
    project.removeRuntimeDependencies(this)

    project.mod.repository.orNull?.let { repository ->
        pom.url = "https://github.com/$repository"

        pom.issueManagement {
            system = "github"
            url = "https://github.com/$repository/issues"
        }

        pom.scm {
            url = "https://github.com/$repository"
            connection = "scm:git:git://github.com/$repository.git"
            developerConnection = "scm:git:git://github.com/$repository.git"
        }
    }

    project.mod.author.orNull?.let { author ->
        pom.developers {
            developer {
                name = author
            }
        }
    }
}
