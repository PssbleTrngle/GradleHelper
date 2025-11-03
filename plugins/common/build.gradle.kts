plugins {
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(project(":core"))
    api(project(":access"))

    api(libs.neoforge.gradle)
    implementation(libs.kotlin.serialization.json)
}

gradlePlugin {
    plugins {
        named(project.name) {
            implementationClass = "com.possible_triangle.gradle.common.GradleHelperCommonPlugin"
        }
    }
}