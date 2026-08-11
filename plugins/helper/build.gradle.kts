plugins {
    alias(libs.plugins.blossom)
}

val majorVersion: String by extra
val isDev: Boolean by extra

dependencies {
    api(project(":metadata"))
}

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
        property("isDev", isDev.toString())
    }
}
