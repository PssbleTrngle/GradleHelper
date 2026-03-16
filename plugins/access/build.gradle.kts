dependencies {
    implementation(libs.forge.renamer)
    implementation(libs.forge.srg)
}

gradlePlugin {
    plugins {
        named(project.name) {
            implementationClass = "com.possible_triangle.gradle.access.AccessWidenerTransformationPlugin"
            description = "converts access wideners to access transformers"
        }
    }
}