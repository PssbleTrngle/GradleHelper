package com.possible_triangle.gradle.features

import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.mod
import org.sonarqube.gradle.SonarExtension
import org.sonarqube.gradle.SonarProperties
import org.sonarqube.gradle.SonarQubePlugin

fun Project.configureSonarQube(block: SonarProperties.() -> Unit) {
    allprojects {
        apply<SonarQubePlugin>()
    }

    allprojects {
        configure<SonarExtension> {
            properties {
                file("src/main").takeIf { it.exists() }?.let {
                    property("sonar.sources", it)
                }
                file("src/test").takeIf { it.exists() }?.let {
                    property("sonar.tests", it)
                }
            }
        }
    }

    configure<SonarExtension> {
        properties {
            val version = mod.minecraftVersion.map { "$it-${mod.version.get()}" }.orElse(mod.version)
            property("sonar.projectVersion", version.get())
            property("sonar.projectKey", mod.id.get())
            mod.repository.orNull?.let { property("sonar.links.scm", it) }

            block(this@properties)
        }
    }
}