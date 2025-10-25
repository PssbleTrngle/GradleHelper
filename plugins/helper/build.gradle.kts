plugins {
    alias(libs.plugins.blossom)
}

val majorVersion: String by extra
val pluginVersion: String by extra

gradlePlugin {
    plugins {
        named(project.name) {
            version = majorVersion
            implementationClass = "com.possible_triangle.gradle.settings.GradleHelperSettingsPlugin"
        }
    }
}

sourceSets.main {
    blossom.kotlinSources {
        property("majorVersion", majorVersion)
    }
}