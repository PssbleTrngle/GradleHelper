package org.gradle.kotlin.dsl

import com.diffplug.gradle.spotless.SpotlessExtension
import com.possible_triangle.gradle.ProjectEnvironment
import com.possible_triangle.gradle.features.configureSonarQube
import com.possible_triangle.gradle.features.configureSpotless
import com.possible_triangle.gradle.features.enableKotlin
import com.possible_triangle.gradle.upload.addGithubPackages
import com.possible_triangle.gradle.upload.addNexus
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.sonarqube.gradle.SonarProperties
import com.possible_triangle.gradle.env as projectEnv

fun Project.withKotlin() = enableKotlin()

val env get(): ProjectEnvironment = projectEnv

fun RepositoryHandler.githubPackages(project: Project, block: MavenArtifactRepository.() -> Unit = {}) =
    addGithubPackages(project, block)

fun RepositoryHandler.githubPackages(repository: String, block: MavenArtifactRepository.() -> Unit = {}) =
    addGithubPackages(repository, block)

fun RepositoryHandler.nexus(type: String = "public", block: MavenArtifactRepository.() -> Unit = {}) =
    addNexus(type, block)

fun Project.enableSonarQube(block: SonarProperties.() -> Unit = {}) = configureSonarQube(block)

fun Project.enableSpotless(enableHook: Boolean = true, block: SpotlessExtension.() -> Unit = {}) =
    configureSpotless(enableHook, block)