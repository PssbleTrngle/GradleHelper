dependencies {
    api(project(":core"))
    api(project(":access"))

    api(libs.forge.legacy.gradle)
}

gradlePlugin {
    plugins {
        named(project.name) {
            implementationClass = "com.possible_triangle.gradle.forge.GradleHelperForgePlugin"
        }
    }
}