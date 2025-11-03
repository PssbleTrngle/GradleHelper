dependencies {
    api(project(":publishing"))

    api(libs.cursegradle)
    api(libs.minotaur)

    api(libs.sonar.scanner)
    api(libs.spotless)
}

gradlePlugin {
    plugins {
        named(project.name) {
            implementationClass = "com.possible_triangle.gradle.GradleHelperCorePlugin"
        }
    }
}