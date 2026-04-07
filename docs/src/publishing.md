## Maven

The `maven-publish` plugin is automatically applied and a maven artifact is configured.

within the `upload` extension, there are a few helper methods to configure repositories commonly used by me.

Everything is also published to `mavenLocal` by default, there is no need to do the specifically.

```kotlin
upload {
   // publishes to https://registry.somethingcatchy.net
   // uses the NEXUS_USER and NEXUS_TOKEN variables
   nexus()
   // publishes to the github packages maven registry
   // uses the GITHUB_ACTOR and GITHUB_TOKEN env variables
   githubPackages()

   repositories {
      // additional maven repositories
      maven(url = uri("..."))
   }
}
```

### Artifact Modifications

There are some modifications that are applied to the artifact's metadata:

- disables [module metadata](https://docs.gradle.org/current/userguide/publishing_gradle_module_metadata.html) generation. This is an additional file that will be preferred over the POM, but is known to cause issues within mod development setups.
- _for forge projects_: remove POM dependencies completely, as they are not working transitivily at all in ForgeGradle 6
- _for non-forge projects_: remove _runtime_ dependencies. These are usually things like [JEI](https://modrinth.com/mod/jei) that are only used for testing in the dev environment and should not be included transitively by other mods. The [table below](#dependency-types) visualizes the different types of dependencies

The `com.possible-triangle.publishing` plugin, that is resposible for these modifications also exposes the helper method `MavenPublication.removePomDependencies`, which can be used to additionally exclude certain dependencies from the __POM__ by _group_, _artifact id_, _scope_ (1), or _version_
{ .annotate }

1.  like _runtime_ or _compile_

### Dependency Types

You can read about what dependency configuration to use at what time and which are represented in the __POM__ in the following table:

| Configuration     | Description                                               | Examples | Included Transitively |
| ----------------- | --------------------------------------------------------- | -------- | ------------------------- |
| modApi            | required dependencies that are needed to complile & run the mod | [Blueprint](https://modrinth.com/mod/blueprint), [Registrate](https://github.com/tterrag1098/Registrate), [Moonlight Lib](https://modrinth.com/mod/moonlight) | :fontawesome-solid-check: |
| modImplementation | optional dependencies that should be there for compile & runtime | mods to have compatiblity with | :fontawesome-solid-x:     |
| modCompileOnly    | optional dependencies that are needed only at compile time | mods to have compatiblity with | :fontawesome-solid-x:     |
| modCompileOnlyApi | required dependencies that are only needed at compile time | _no idea_ | :fontawesome-solid-check:     |
| modRuntimeOnly    | optional dependencies that are only needed at runtim | mods to have compatiblity with or help within the dev environment like [JEI](https://modrinth.com/mod/jei) | :fontawesome-solid-x:     |

## Uploading

When running the `publish` task, the project will automatically be uploaded to modrinth & curseforge, if the required values are set. These values are described in their individual blocks below.

To overwrite specific values, like the _version name_, the _changelog_ or define dependencies, you can use the the common API within the `upload` extension.

To plugin will automatically choose the correct jar artifact. That could be the normal one, or the one including [Jar-In-Jar](https://docs.minecraftforge.net/en/fg-6.x/dependencies/jarinjar/) dependencies, if any are configured.

```kotlin
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

### Modrinth

Under the hood the [minotaur](https://github.com/modrinth/minotaur) plugin is used to publish build artifacts to modrinth.

This __only__ happens if the `MODRINTH_TOKEN` environment variable and the `modrinth_project_id` gradle property are set.

### Curseforge

Under the hood the [CurseForgeGradle](https://github.com/Darkhax/CurseForgeGradle) plugin is used (1) to publish build artifacts to curseforge.
{ .annotate }

1.   specifically a [fork](https://github.com/PssbleTrngle/CurseForgeGradle) that fixed a few issues

This __only__ happens if the `CURSFORGE_TOKEN` environment variable and the `curseforge_project_id` gradle property are set.