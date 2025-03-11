plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    publishing
    alias(libs.plugins.spotless)
    alias(libs.plugins.sonar)
    jacoco
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
    maven { url = uri("https://maven.minecraftforge.net") }
    maven { url = uri("https://maven.fabricmc.net/") }
    maven { url = uri("https://maven.neoforged.net/releases/") }
    maven { url = uri("https://plugins.gradle.org/m2/") }
}

val plugin_id: String by extra
val artifact_id: String by extra
val plugin_version: String by extra
val repository: String by extra

configurations.all {
    resolutionStrategy {
        force("com.google.code.gson:gson:2.11.0")
        force("org.codehaus.groovy:groovy-all:3.0.21")
    }
}

dependencies {
    api(libs.kotlin.gradle)
    api(libs.kotlin.serialization)

    api(libs.cursegradle)
    api(libs.minotaur)

    api(libs.fabric.loom.gradle)
    api(libs.forge.gradle)
    api(libs.neoforge.gradle)
    api(libs.vanilla.gradle)

    api(libs.mixin.gradle)

    api(libs.sonar.scanner)
    api(libs.spotless)

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation(libs.junit.snapshots)
}

gradlePlugin {
    vcsUrl.set("https://github.com/${repository}")
    website.set(vcsUrl)

    plugins {
        create("gradle-helper") {
            id = plugin_id
            version = plugin_version
            implementationClass = "com.possible_triangle.gradle.GradleHelperPlugin"
            displayName = "Gradle Helper"
            description =
                "bundles fabric/forge/common gradle plugins and provides useful default configurations for minecraft mod developers"
            tags.set(setOf("minecraft", "forge", "fabricmc", "loom"))
        }
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

val env: Map<String, String> = System.getenv()
publishing {
    repositories {
        env["LOCAL_MAVEN"]?.let { localMaven ->
            maven {
                name = "local-maven"
                url = uri(localMaven)
            }
        }

        env["GRADLE_PUBLISH_KEY"]?.let {
            gradlePluginPortal {
                name = "gradle-plugin-portal"
            }
        }
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