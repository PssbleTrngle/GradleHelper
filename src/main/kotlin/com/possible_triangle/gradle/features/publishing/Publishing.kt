package com.possible_triangle.gradle.features.publishing

import com.possible_triangle.gradle.env
import com.possible_triangle.gradle.features.loaders.isSubProject
import com.possible_triangle.gradle.mod
import groovy.util.Node
import groovy.util.NodeList
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get

fun RepositoryHandler.githubPackages(project: Project) {
    maven {
        name = "GitHubPackages"
        url = project.uri("https://maven.pkg.github.com/${project.mod.repository.get()}")
        credentials {
            username = project.env["GITHUB_ACTOR"]
            password = project.env["GITHUB_TOKEN"]
        }
    }
}

private fun Project.defaultArtifactName(): String {
    return if(isSubProject) "${mod.id.get()}-${name.lowercase()}"
    else mod.id.get()
}

fun Project.enablePublishing(
    artifactVersion: String = mod.version.get(),
    group: String = mod.mavenGroup.get(),
    name: String =  defaultArtifactName(),
    block: PublishingExtension.() -> Unit,
) {
    apply<MavenPublishPlugin>()

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("gpr") {
                groupId = group
                artifactId = name
                version = artifactVersion
                from(components["java"])

                pom.withXml {
                    val node = asNode()
                    val list = node.get("dependencies") as NodeList
                    list.forEach { node.remove(it as Node) }
                }
            }
        }
        block()
    }
}