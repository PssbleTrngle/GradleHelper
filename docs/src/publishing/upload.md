# Uploading

When running the `publish` task, the project will automatically be uploaded to modrinth & curseforge, if the required values are set. These values are described in their individual blocks below.

To overwrite specific values, like the _version name_, the _changelog_ or define dependencies, you can use the the common API within the `upload` extension.

To plugin will automatically choose the correct jar artifact. That could be the normal one, or the one including [Jar-In-Jar](https://docs.minecraftforge.net/en/fg-6.x/dependencies/jarinjar/) dependencies, if any are configured.

```kotlin title="build.gradle.kts"
upload {
    // if possible, always prefer "forEach" to configure both modrinth & curseforge at the same time
    curseforge {
        dependencies {
            // for curseforge, the second (optional) parameter can be set the concrete project ID
            // this is required for some mods like blueprint, because there is also a modpack with the same name
            // which will be instead resolved incorrectly if the ID is omitted
            required("blueprint", 382216)
        }
    }
    modrinth {
        dependencies {
            // for modrinth, the slug is unique and will work as an identifier always
            required("blueprint")
        }
    }

    forEach {
        versionName = "Some Other Version Name ${mod.version.get()}"
    }
}
```

## Modrinth

Under the hood the [minotaur](https://github.com/modrinth/minotaur) plugin is used to publish build artifacts to modrinth.

This __only__ happens if the `MODRINTH_TOKEN` environment variable and the `modrinth_project_id` gradle property are set.

## Curseforge

Under the hood the [CurseForgeGradle](https://github.com/Darkhax/CurseForgeGradle) plugin is used (1) to publish build artifacts to curseforge.
{ .annotate }

1.   specifically a [fork](https://github.com/PssbleTrngle/CurseForgeGradle) that fixed a few issues

This __only__ happens if the `CURSFORGE_TOKEN` environment variable and the `curseforge_project_id` gradle property are set.