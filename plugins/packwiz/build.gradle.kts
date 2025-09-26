plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(libs.kotlin.serialization)
    api(libs.kotlin.toml)
}

val plugin_id: String by extra
val plugin_version: String by extra

gradlePlugin {
    plugins {
        create("packwiz") {
            id = "$plugin_id.packwiz"
            version = plugin_version
            implementationClass = "com.possible_triangle.gradle.packwiz.PackwizVersionCatalogPlugin"
            displayName = "Gradle Helper"
            description =
                "bundles fabric/forge/common gradle plugins and provides useful default configurations for minecraft mod developers"
            tags.set(setOf("minecraft", "forge", "fabricmc", "loom"))
        }
    }
}