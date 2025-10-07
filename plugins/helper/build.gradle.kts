plugins {
    alias(libs.plugins.blossom)
}

gradlePlugin {
    plugins {
        named(project.name) {
            implementationClass = "com.possible_triangle.gradle.settings.GradleHelperSettingsPlugin"
        }
    }
}

val pluginVersion: String by extra

sourceSets.main {
    blossom.kotlinSources {
        property("version", pluginVersion)
    }
}