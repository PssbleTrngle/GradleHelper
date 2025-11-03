dependencies {
    implementation(libs.architectury.loom)
}

gradlePlugin {
    plugins {
        named(project.name) {
            implementationClass = "com.possible_triangle.gradle.publishing.GradleHelperPublishingPlugin"
        }
    }
}