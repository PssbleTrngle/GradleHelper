plugins {
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(project(":core"))
    api(project(":access"))

    api(libs.forge.gradle)
    api(libs.mixin.gradle)
    api(libs.kotlin.serialization.json)
}

gradlePlugin {
    plugins {
        named(project.name) {
            implementationClass = "com.possible_triangle.gradle.forge.GradleHelperForgePlugin"
        }
    }
}