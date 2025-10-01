import com.gradle.publish.PublishPlugin

plugins {
    `kotlin-dsl`
    jacoco
    alias(libs.plugins.plugin.publish) apply (false)
    alias(libs.plugins.spotless)
    alias(libs.plugins.sonar)
}

val repository: String by extra

allprojects {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}

val env: Map<String, String> = System.getenv()

subprojects {
    apply<PublishPlugin>()

    gradlePlugin {
        vcsUrl.set("https://github.com/$repository")
        website.set(vcsUrl)
    }

    configure<PublishingExtension> {
        repositories {
            mavenLocal()
            env["GRADLE_PUBLISH_KEY"]?.let {
                gradlePluginPortal {
                    name = "gradle-plugin-portal"
                }
            }
        }
    }

    dependencies {
        api(rootProject.libs.kotlin.gradle)

        testImplementation(rootProject.libs.kotlin.test)
        testImplementation(rootProject.libs.junit.snapshots)
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
    dependsOn(subprojects.map { it.tasks["publishPlugins"] })
}
