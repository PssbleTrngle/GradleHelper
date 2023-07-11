package com.possible_triangle.gradle.features

import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.mod
import org.sonarqube.gradle.SonarExtension
import org.sonarqube.gradle.SonarProperties
import org.sonarqube.gradle.SonarQubePlugin

fun Project.configureSonarQube(block: Project.(SonarProperties) -> Unit) {
    allprojects {
        apply<SonarQubePlugin>()
    }

    configure<SonarExtension> {
        properties {
            property("sonar.projectVersion", mod.version)
            property("sonar.projectKey", mod.id)

            block(this@properties)
        }
    }

    subprojects {
        configure<SonarExtension> {
            properties {
                property("sonar.branch", this@subprojects.name)

                block(this@properties)
            }
        }
    }
}