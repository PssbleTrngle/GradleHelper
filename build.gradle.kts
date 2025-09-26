plugins {
    `kotlin-dsl`
    alias(libs.plugins.plugin.publish)
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
    apply(
        plugin =
            rootProject.libs.plugins.plugin.publish
                .get()
                .pluginId,
    )

    gradlePlugin {
        vcsUrl.set("https://github.com/$repository")
        website.set(vcsUrl)
    }

    publishing {
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
