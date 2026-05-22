# Whats Changes

## New Upload Task

Instead of running `modrinth` and `curseforge` when calling `publish`,
these three tasks now exist independ from eachother, allowing users to publish only to maven.
This can be useful if modrinth or curseforge completed successfully, allowing users to only re-run the failing parts.

There is now a new `upload` task, that runs all of these three in a specific order.

```sh title="example project structure"
└── project/
    ├── settings.gradle.kts
    ├── build.gradle.kts
    ├── common/
    │   └── build.gradle.kts
    ├── fabric/
    │   └── build.gradle.kts
    └── neoforge/
        └── build.gradle.kts

# tasks will be ran in the following order
:upload
   :curseforge
      :fabric:curseforge
      :neoforge:curseforge
   :modrinth
      :fabric:modrinth
      :neoforge:modrinth
   :publish
      :common:publish
      :fabric:publish
      :neoforge:publish
```

## Enabled Module Metadata Generation

[Module Metadata](https://blog.gradle.org/gradle-metadata-1.0) was previously disabled, as it was known to cause problems in modded environments, especially with ForgeGradle.
It has now been enabled again and will receive similar [modifications](/publishing#artifact-modifications) like the POM has already been receiving.

This means runtime-only dependencies will be removed from there. For [forge](/mod-loaders/forge) it will remain disabled.

It is used for example by NeoForge's ModDevGradle to [publish & include transitive Access Transformers](https://github.com/neoforged/ModDevGradle#access-transformers)

## Detect required Java Version

The `java_version` property no longer defaults to `21`, but will not be set according the the specified Minecraft version.

| Minecraft Version | Java Version |
| ----------------- | ------------ |
| ≤ 1.20.1          | 17           |
| 1.21.*            | 21           |
| ≥ 26.1            | 26           |

## Unit-Test helper for NeoForge

The `neoforge` extension has a new `unitTests` property. When set to `true`,
it will enable & setup the [NeoForge Unit Tests](https://github.com/neoforged/ModDevGradle#unit-testing-with-junit).
No further setup is required, the required test dependencies are already added by the plugin.
