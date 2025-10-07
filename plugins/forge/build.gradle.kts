dependencies {
    api(project(":core"))

    api(libs.forge.gradle)
    api(libs.mixin.gradle)
}

gradlePlugin {
    plugins {
        named(project.name) {
            implementationClass = "com.possible_triangle.gradle.forge.GradleHelperForgePlugin"
        }
    }
}