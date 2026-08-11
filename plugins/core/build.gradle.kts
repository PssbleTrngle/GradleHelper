plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.blossom)
}

dependencies {
    api(project(":publishing"))
    api(project(":metadata"))

    api(libs.kotlin.gradle)
    api(libs.kotlin.serialization.json)

    api(libs.cursegradle)
    api(libs.minotaur)

    api(libs.sonar.scanner)
    api(libs.spotless)
}

val pluginVersion: String by extra

sourceSets.main {
    blossom.kotlinSources {
        property("pluginVersion", pluginVersion)
    }
}

gradlePlugin {
    plugins {
        named(project.name) {
            implementationClass = "com.possible_triangle.gradle.GradleHelperCorePlugin"
        }
    }
}
