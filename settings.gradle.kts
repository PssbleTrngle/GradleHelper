val plugin_id: String by extra

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

val dir = file("plugins")
dir.list().forEach {
    include(":$it")
    project(":$it").projectDir = dir.resolve(it)
}

rootProject.name = "Gradle Plugins"