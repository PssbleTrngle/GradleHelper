# Minecraft Gradle Helper Plugin

This plugin is intended to simplify the gradle workflow and project boilerplate code of minecraft mod projects.
It works for fabric, forge or even multiloader projects containing multiple subprojects.

## Usage

Add the following to `settings.gradle` or `settings.gradle.kts`
```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = uri("https://maven.minecraftforge.net/") }
        maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
    }
}
```

The plugin only has to be applied in the root project using the plugins dsl block in the `build.gradle.kts`

```kotlin
plugins {
    id("net.somethingcatchy.gradle") version (VERSION)
}
```

## Configuration

Using the `mod` block, projects can be configured on root level. Single properties can also be overwritten on subproject level.
The default values for these will be extracted from the `gradle.properties` file if not specified manually.

```kotlin
mod {
    id.set("example-mod") // default: mod_id in gradle.properties
    name.set("Example Mod") // default: mod_name
    version.set("1.0") // default: mod_version
    minecraftVersion.set("1.20.1") // default: mc_version
}
```

## Common / Fabric / Forge setup

For single-loader projects, only one of these directives will be added in the *rootProject*. 
For multiloader projects, these will be applied in the corresponding projects `build.gradle.kts`

## Kotlin Support

the `withKotlin` directives automatically applies the kotlin jvm & serialization plugins to this and all subprojects.
By specified a `kotlin_fabric_version` or `kotlin_forge_version` in the `gradle.properties` file or by setting them manually in the corresponding loader directive,
These mod dependencies will also be added to the source code and curseforge/modrinth upload configurations

## Publishing

The `uploadToCurseforge` and `uploadToModrinth` apply and configure the necessary plugins to upload the mod to these platforms using gradle.
Most of the default values are grabbed from the `mod` extension and thereby the gradle properties.