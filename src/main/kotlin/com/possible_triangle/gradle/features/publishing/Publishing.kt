package com.possible_triangle.gradle.features.publishing

import com.possible_triangle.gradle.features.detectKotlin
import com.possible_triangle.gradle.features.loaders.isSubProject
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

fun RepositoryHandler.addNexus(snapshot: Boolean, block: MavenArtifactRepository.() -> Unit) {
    val repository = if (snapshot) "maven-snapshots" else "maven-releases"
    val token = env["NEXUS_TOKEN"]
    val user = env["NEXUS_USER"]

    maven {
        name = "Nexus"
        url = URI("https://registry.somethingcatchy.net/repository/$repository/")

        if (token != null || user != null) {
            credentials {
                username = user
                password = token
            }
        }

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
}

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

fun MavenPublication.overwriteDependencies() {
    pom.withXml {
        val node = asNode()
        val list = node.get("dependencies") as NodeList
        list.forEach { node.remove(it as Node) }
    }
}