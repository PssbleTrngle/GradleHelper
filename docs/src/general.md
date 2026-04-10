## Java Setup

the `java_version` _gradle.properties_ variable will be used to configure the Java gradle plugin.
It defaults to **21**

## Environment Variables

Values defined in a `.env.local` file in the projects root directory will also be loaded and used by any plugin that uses environment variables.
These can be accessed in a map called `env`, that is a combination of these local and the "normal" environment variables.

## Mod Properties

Several mod properties can be configured and read using the `mod` extension.
The are inherited to sub-project and are used for the various features these plugins implement.

Some of these values can be referenced in mod metadata files, namingly `mods.toml`, `neoforge.mods.toml`, `fabric.mod.json` and any `<mod-id>*.mixins.json` file. Those fields have a _template_ in the table below.

| Property             | Default Value                                                          | Format                                 | Template               |
| -------------------- | ---------------------------------------------------------------------- | -------------------------------------- | ---------------------- |
| mod.id               | _gradle.properties_: `mod_id`                                          | _string_                               | `${mod_id}`            |
| mod.name             | _gradle.properties_: `mod_name`                                        | _string_                               | `${mod_name}`          |
| mod.version          | _environment_: `RELEASE_VERSION` or _gradle.properties_: `mod_version` | _string_                               | `${mod_version}`       |
| mod.author           | _gradle.properties_: `mod_author`                                      | _string_                               | `${mod_author}`        |
| mod.description      | _gradle.properties_: `mod_description`                                 | _string_                               | `${mod_description}`   |
| mod.minecraftVersion | _gradle.properties_: `minecraft_version` or `mc_version`               | _string_                               | `${minecraft_version}` |
| mod.releaseType      | _gradle.properties_: `release_type`                                    | should be `release`, `beta` or `alpha` | -                      |
| mod.repository       | _gradle.properties_: `repository`                                      | owner/repository                       | `${repository}`        |
| mod.mavenGroup       | _gradle.properties_: `maven_group`                                     | _string_                               | -                      |

There are also a number of additional template expressions available:

| Template                     | Value           | Example     |
| ---------------------------- | --------------- | ----------- |
| `${minecraft_version_range}` | `[mc_version,)` | `[1.20.1,)` |

You can also define additional values that should be replaced in these files:

```kotlin title="build.gradle.kts"
mod {
    additional.add("some_key", "some_value")
    additional.add("other_key", provider { "other_value" })
}
```

```toml title="neoforge.mods.toml"
[[mods]]
modId = "${mod_id}"
itemIcon = "${some_key}"
```

## Including Libraries

Different mod loaders provide different options allowing a developer to bundle libraries or other mods within them.

All loader plugins provide a common API to specify these included dependencies.

Included mods and libraries defined in the root project are inherited in the sub-projects.
This can be helpful for libraries that are loader-agnostic.

```kotlin title="neoforge/build.gradle.kts"
mod {
   // bundle mods
    mods.include("com.tterrag.registrate_fabric:Registrate:$registrate_version")
    // also works with version catalogs
    mods.include(libs.registrate.neoforge)

    // or non-mod libraries
    libraries.include("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
}
```

## Datagen

!!! note
     This plugin ecosystem allows to more easily configure datagen, but also follows some standard setups that may not be what you want.
     As always, any custom configuration can still be done, as all bundled and applied loader plugins can still be configured.

Any of the `neoforge`, `forge` or `fabric` plugins provide a common API in their respective extensions.
The example below uses neoforge but can also be used for any of the other two.

For loader-specific functionality you can read about in the respective page.

```kotlin title="neoforge/build.gradle.kts"
neoforge {
    // enable datagen for this project
    // this will create a "data" run configuration
    dataGen {
        // configure existing mods whose assets have to be present
        existing("blueprint")
        
        // can be used to use a different source set for the datagen code
        // this can be useful, as it will not be included in the published JAR
        sourceSet(project.sourceSets.other)
        // creates and uses a "data" source set
        splitSourceSet()
    }
}
```

The generated assets will be placed in `src/generated/resources`.