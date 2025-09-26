package com.possible_triangle.gradle.features

import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import com.possible_triangle.gradle.mod
import org.sonarqube.gradle.SonarExtension
import org.sonarqube.gradle.SonarProperties
import org.sonarqube.gradle.SonarQubePlugin

fun Project.configureSonarQube(block: SonarProperties.() -> Unit) {
    allprojects {
        apply<SonarQubePlugin>()
    }

    configure<SonarExtension> {
        properties {
            val version = mod.minecraftVersion.map { "$it-${mod.version.get()}" }.orElse(mod.version)
            property("sonar.projectVersion", version.get())
            property("sonar.projectKey", mod.id.get())
            property("sonar.gradle.skipCompile", "true")
            mod.repository.orNull?.let { property("sonar.links.scm", it) }

            block(this@properties)
        }
    }
}