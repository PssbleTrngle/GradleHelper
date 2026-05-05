dependencies {
    implementation(libs.forge.srg)
    implementation(libs.forge.gradle)

    implementation(libs.forge.legacy.gradle)
    implementation(libs.neoforge.srg)
}

gradlePlugin {
    plugins {
        named(project.name) {
            implementationClass = "com.possible_triangle.gradle.access.AccessWidenerTransformationPlugin"
            description = "converts access wideners to access transformers"
        }
    }
}
