import com.gradle.publish.PublishPlugin

plugins {
    `kotlin-dsl`
    jacoco
    alias(libs.plugins.plugin.publish) apply (false)
    alias(libs.plugins.spotless)
    alias(libs.plugins.sonar)
}

val env: Map<String, String> = System.getenv()

val repository: String by extra
val plugin_id: String by extra

val major_version: String by extra
val patch = env["PATCH"] ?: "999"
val pluginVersion = "$major_version.$patch"

allprojects {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}

allprojects {
    configurations.all {
        resolutionStrategy {
            force("com.google.code.gson:gson:2.11.0")
            force("org.codehaus.groovy:groovy-all:3.0.24")
        }
    }
}

fun pluginProjects(block: Project.() -> Unit) {
    subprojects
        .filter { it.projectDir.relativeTo(it.rootDir).startsWith("plugins/") }
        .forEach { it.block() }
}

pluginProjects {
    apply<PublishPlugin>()
    apply<KotlinDslPlugin>()

    gradlePlugin {
        vcsUrl.set("https://github.com/$repository")
        website.set(vcsUrl)

        plugins {
            create(project.name) {
                id = "$plugin_id.${project.name}"
                version = pluginVersion
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
            mavenLocal()

            val nexusToken = env["NEXUS_TOKEN"]
            val nexusUser = env["NEXUS_USER"]
            if (nexusToken != null && nexusUser != null) {
                maven {
                    url = uri("https://registry.somethingcatchy.net/repository/maven-releases/")
                    credentials {
                        username = nexusUser
                        password = nexusToken
                    }
                }
            }

            if (env["GRADLE_PUBLISH_KEY"] != null) {
                gradlePluginPortal {
                    name = "gradle-plugin-portal"
                }
            }
        }
    }

    repositories {
        maven {
            url = uri("https://registry.somethingcatchy.net/repository/maven-public/")
        }
    }

    dependencies {
        api(rootProject.libs.kotlin.gradle)
        api(rootProject.libs.kotlin.serialization)

        testImplementation(rootProject.libs.kotlin.test)
        testImplementation(project(":test"))
    }
}

spotless {
    kotlin {
        ktlint()
        leadingTabsToSpaces()
        suppressLintsFor { shortCode = "standard:package-name" }
        suppressLintsFor { shortCode = "standard:no-wildcard-imports" }
    }
    kotlinGradle {
        ktlint()
        suppressLintsFor { shortCode = "standard:property-naming" }
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

tasks.register("publishPlugins") {
    pluginProjects {
        dependsOn(tasks["publishPlugins"])
    }
}
