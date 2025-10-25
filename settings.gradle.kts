pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

fun include(dir: File) {
    dir.list().forEach {
        include(":$it")
        project(":$it").projectDir = dir.resolve(it)
    }
}

include(file("plugins"))
include(file("modules"))

rootProject.name = "Gradle Plugins"
