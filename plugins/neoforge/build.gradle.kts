dependencies {
    api(project(":core"))

    api(libs.neoforge.gradle)
}

gradlePlugin {
    plugins {
        named(project.name) {
            implementationClass = "com.possible_triangle.gradle.neoforge.GradleHelperNeoForgePlugin"
        }
    }
}