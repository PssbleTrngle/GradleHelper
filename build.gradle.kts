import com.diffplug.gradle.spotless.SpotlessPlugin
import com.google.gson.Gson
import com.gradle.publish.PublishPlugin

plugins {
    `kotlin-dsl`
    jacoco
    alias(libs.plugins.plugin.publish) apply (false)
    alias(libs.plugins.spotless)
    alias(libs.plugins.sonar)
    idea
}

val env: Map<String, String> = System.getenv()

val repository: String by extra
val pluginId: String by extra

val isCI = env["CI"] == "true"
val majorVersion =
    if (isCI) {
        project.extra["major_version"].toString()
    } else {
        "99.0"
    }

val isSnapshot = env["SNAPSHOT"] == "true"
val isRelease = env["RELEASE"] == "true"
val patch = env["PATCH"] ?: "999"
val pluginVersion = "$majorVersion.$patch"

allprojects {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }

    configurations.all {
        resolutionStrategy {
            force("org.codehaus.groovy:groovy-all:3.0.24")
        }
    }
}

fun pluginProjects(block: Project.() -> Unit) {
    subprojects
        .filter { it.projectDir.relativeTo(it.rootDir).startsWith("plugins/") }
        .forEach { it.block() }
}

val repositoryUrl = "https://github.com/$repository"
val cloneUrl = "scm:git:git://github.com/$repository.git"

pluginProjects {
    apply<PublishPlugin>()
    apply(plugin = "org.gradle.kotlin.kotlin-dsl")

    extra["pluginVersion"] = pluginVersion
    extra["majorVersion"] = majorVersion
    extra["snapshot"] = isSnapshot

    gradlePlugin {
        vcsUrl.set(repositoryUrl)
        website.set(repositoryUrl)

        plugins {
            create(project.name) {
                id = "$pluginId.${project.name}"
                version = if (isSnapshot) "$majorVersion-SNAPSHOT" else pluginVersion
                displayName = "Gradle Helper"
                implementationClass = "replaced in subprojects"
                description =
                    "bundles fabric/forge/common gradle plugins and provides useful default configurations for minecraft mod developers"
                tags.set(setOf("minecraft", "forge", "fabricmc", "loom"))
            }
        }
    }

    configure<PublishingExtension> {
        repositories {
            val nexusToken = env["NEXUS_TOKEN"]
            val nexusUser = env["NEXUS_USER"]
            if (nexusToken != null && nexusUser != null) {
                maven {
                    name = "nexus"
                    val type = if (isSnapshot) "snapshots" else "releases"
                    url = uri("https://registry.somethingcatchy.net/repository/maven-$type/")
                    credentials {
                        username = nexusUser
                        password = nexusToken
                    }
                }
            }

            if (env["GRADLE_PUBLISH_KEY"] != null && !isSnapshot) {
                gradlePluginPortal {
                    name = "gradle-plugin-portal"
                }
            }
        }

        publications.withType<MavenPublication> {
            pom.url = repositoryUrl

            pom.scm {
                url = repositoryUrl
                connection = cloneUrl
                developerConnection = cloneUrl
            }

            pom.developers {
                developer {
                    name = "possible_triangle"
                    url = "https://github.com/PssbleTrngle"
                }
            }

            pom.licenses {
                license {
                    name = "MIT License"
                    url = "https://www.opensource.org/licenses/mit-license.php"
                }
            }
        }
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            url = uri("https://registry.somethingcatchy.net/repository/maven-public/")
        }
    }

    dependencies {
        api(rootProject.libs.kotlin.gradle)
        api(rootProject.libs.kotlin.serialization)
        implementation(rootProject.libs.gson)

        testImplementation(rootProject.libs.kotlin.test)
        testImplementation(project(":test"))
    }
}

allprojects {
    apply<SpotlessPlugin>()

    spotless {
        kotlin {
            ktlint()
            targetExclude("build/generated/**")
        }
        kotlinGradle {
            ktlint()
        }
    }
}

sonar {
    properties {
        property("sonar.projectKey", "gradle-helper")
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
    }
}

val generateReleaseMetadata =
    tasks.register("generateReleaseMetadata") {
        val properties =
            mapOf(
                "tag" to pluginVersion,
            )

        val output = layout.buildDirectory.file("release.json")

        outputs.file(output)
        inputs.properties(properties)

        doFirst {
            val json = Gson().toJson(properties)
            output.get().asFile.writeText(json)
        }

        onlyIf { !isSnapshot }
    }

tasks.register("publishAll") {
    pluginProjects {
        val isHelper = project.name == "helper"
        if (isHelper && (isSnapshot || !isRelease)) return@pluginProjects

        if (isSnapshot) {
            dependsOn(tasks["publish"])
        } else {
            dependsOn(tasks["publishPlugins"])
        }
    }

    finalizedBy(generateReleaseMetadata)
}

idea {
    module {
        excludeDirs.add(file("docs"))
    }
}
