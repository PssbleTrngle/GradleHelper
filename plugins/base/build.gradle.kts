plugins {
    `kotlin-dsl`
    jacoco
}

configurations.all {
    resolutionStrategy {
        force("com.google.code.gson:gson:2.11.0")
        force("org.codehaus.groovy:groovy-all:3.0.24")
    }
}

repositories {
    maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
    maven { url = uri("https://maven.minecraftforge.net") }
    maven { url = uri("https://maven.fabricmc.net/") }
    maven { url = uri("https://maven.neoforged.net/releases/") }
}

dependencies {
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

    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.snapshots)
}

val plugin_id: String by extra
val plugin_version: String by extra

gradlePlugin {
    plugins {
        create("gradle-helper") {
            id = "$plugin_id.gradle"
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