dependencies {
    api(project(":core"))

    api(libs.fabric.loom.gradle)
}

gradlePlugin {
    plugins {
        named(project.name) {
            implementationClass = "com.possible_triangle.gradle.fabric.GradleHelperFabricPlugin"
        }
    }
}