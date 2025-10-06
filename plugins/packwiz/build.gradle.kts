plugins {
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(libs.kotlin.toml)
}

gradlePlugin {
    plugins {
        named(project.name) {
            implementationClass = "com.possible_triangle.gradle.packwiz.PackwizVersionCatalogPlugin"
            description = "imports packwiz pack metadata as gradle version catelog"
        }
    }
}