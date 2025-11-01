# Minecraft Gradle Helper Plugin

[![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/com.possible-triangle.core?logo=gradle&label=latest%20version)](https://plugins.gradle.org/plugin/com.possible-triangle.helper)

This plugin is intended to simplify the gradle workflow and project boilerplate code of minecraft mod projects.
It works for fabric, forge or even multiloader projects containing multiple subprojects.

## Usage

Add the following to `settings.gradle` or `settings.gradle.kts`
```kotlin
plugins {
    id("com.possible-triangle.helper") version ("<plugin-version>")
}
```

### Multi-Project setup
The plugin should be applied in the root project using the plugins dsl block in the `build.gradle.kts`

```kotlin
plugins {
    id("com.possible-triangle.core")

    // define the plugins used in subprojects to avoid classpath issues
    id("com.possible-triangle.fabric") apply false
    id("com.possible-triangle.neoforge") apply false
    ...
}
```

### Single-Project setup

```kotlin
plugins {
    id("com.possible-triangle.neoforge")
}
```

## Configuration

Using the `mod` block, projects can be configured on root level. Single properties can also be overwritten on subproject level.
The default values for these will be extracted from the `gradle.properties` file if not specified manually.

```kotlin
mod {
    id = "example-mod" // default: mod_id in gradle.properties
    name = "Example Mod" // default: mod_name
    version = "1.0" // default: mod_version
    minecraftVersion = "1.20.1" // default: mc_version
    
    libraries.include("com.example:library:1.0.0")
    libraries.include(libs.the.other.library)
    mods.include("com.example:mods:1.0.0")
}
```

The different loader implementations have extensions that can be used to modify some loader specific values, as well as configure inter-project dependencies

```kotlin
forge {
    enabledMixins()
    forgeVersion = "..."
    dependOn(project(":common"))
}

fabric {
    dataGen()
    dependOn(project(":common"))
}
```

## Kotlin Support

the `withKotlin` directives automatically applies the kotlin jvm & serialization plugins to this and all subprojects.
By specified a `kotlin_fabric_version` or `kotlin_forge_version` in the `gradle.properties` file or by setting them manually in the corresponding loader directive,
These mod dependencies will also be added to the source code and curseforge/modrinth upload configurations

## Publishing

The `upload` extension configures the necessary plugins to upload the mod to modrinth, curseforge and maven platforms using gradle.
Most of the default values are grabbed from the `mod` extension and thereby the gradle properties.
