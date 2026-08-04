# Version Stategies

The `VersionStrategy` that is used defined how the various versions & artifact names are generated from the mod metadata.
It can be customized by setting the `version_strategy` gradle property to a value defined below, or by setting it directly on the `mod` extension.

```kotlin title="neoforge/build.gradle.kts"
mod {
   versionStrategy = MyVersionStrategy()
}
```

## Generated Fields

| Field           | Description                                                                                 |
|-----------------|---------------------------------------------------------------------------------------------|
| modVersion      | replaces `mod_version` in [templates](/general#mod-properties) such as `neoforge.mods.toml` |
| metadataTag     | tag property written to `build/release.json`                                                |
| uploadVersion   | version published to curseforge/modrinth                                                    |
| artifactVersion | version of the maven artifact being published                                               |
| artifactName    | name of the maven artifact being published                                                  |
| baseName        | base name of the JAR file being created                                                     |

## Default Strategy

`version_strategy = simple`

The `SimpleStrategy` will strip the version metadata (everything after the plus sign) from everything except the `metadataVersion`.

=== "mod.version = "1.0.0""

    | Field           | Value          |
    |-----------------|----------------|
    | modVersion      | `1.0.0`        |
    | metadataTag     | `1.0.0`        |
    | uploadVersion   | `1.0.0`        |
    | artifactVersion | `1.0.0`        |
    | artifactName    | `mod_id`       |
    | baseName        | `mod_id-1.0.0` |

=== "mod.version = "1.0.0+some.metadata""

    | Field           | Value                 |
    |-----------------|-----------------------|
    | modVersion      | `1.0.0`               |
    | metadataTag     | `1.0.0+some.metadata` |
    | uploadVersion   | `1.0.0`               |
    | artifactVersion | `1.0.0`               |
    | artifactName    | `mod_id`              |
    | baseName        | `mod_id-1.0.0`        |

=== "mod.version = "1.0.0-prerelease""

    | Field           | Value                     |
    |-----------------|---------------------------|
    | modVersion      | `1.0.0-prerelease`        |
    | metadataTag     | `1.0.0-prerelease`        |
    | uploadVersion   | `1.0.0-prerelease`        |
    | artifactVersion | `1.0.0-prerelease`        |
    | artifactName    | `mod_id`                  |
    | baseName        | `mod_id-1.0.0-prerelease` |

## With-Minecraft-Version Strategy

`version_strategy = with_minecraft_version`

Wraps around the default version strategy and adds the minecraft version to the artifact name & some of the versions metadata.

=== "mod.version = "1.0.0""

    | Field           | Value            |
    |-----------------|------------------|
    | modVersion      | `1.0.0`          |
    | metadataTag     | `1.0.0+mc1.21.1` |
    | uploadVersion   | `1.0.0`          |
    | artifactVersion | `1.0.0`          |
    | artifactName    | `mod_id-1.21.1`  |
    | baseName        | `mod_id-1.0.0`   |

=== "mod.version = "1.0.0+mc1.21.1""

    | Field           | Value            |
    |-----------------|------------------|
    | modVersion      | `1.0.0`          |
    | metadataTag     | `1.0.0+mc1.21.1` |
    | uploadVersion   | `1.0.0`          |
    | artifactVersion | `1.0.0`          |
    | artifactName    | `mod_id-1.21.1`  |
    | baseName        | `mod_id-1.0.0`   |

=== "mod.version = "1.0.0+some.metadata""

    | Field           | Value                          |
    |-----------------|--------------------------------|
    | modVersion      | `1.0.0`                        |
    | metadataTag     | `1.0.0+some.metadata.mc1.21.1` |
    | uploadVersion   | `1.0.0`                        |
    | artifactVersion | `1.0.0`                        |
    | artifactName    | `mod_id-1.21.1`                |
    | baseName        | `mod_id-1.0.0`                 |

=== "mod.version = "1.0.0-prerelease""

    | Field           | Value                       |
    |-----------------|-----------------------------|
    | modVersion      | `1.0.0-prerelease`          |
    | metadataTag     | `1.0.0-prerelease+mc1.21.1` |
    | uploadVersion   | `1.0.0-prerelease`          |
    | artifactVersion | `1.0.0-prerelease`          |
    | artifactName    | `mod_id-1.21.1`             |
    | baseName        | `mod_id-1.0.0-prerelease`   |

## With-Loader Strategy

`version_strategy = with_loader`

=== "mod.version = "1.0.0""

    | Field           | Value                   |
    |-----------------|-------------------------|
    | modVersion      | `1.0.0`                 |
    | metadataTag     | `1.0.0+neoforge`        |
    | uploadVersion   | `1.0.0`                 |
    | artifactVersion | `1.0.0`                 |
    | artifactName    | `mod_id-neoforge`       |
    | baseName        | `mod_id-neoforge-1.0.0` |

=== "mod.version = "1.0.0+neoforge""

    | Field           | Value                   |
    |-----------------|-------------------------|
    | modVersion      | `1.0.0`                 |
    | metadataTag     | `1.0.0+neoforge`        |
    | uploadVersion   | `1.0.0`                 |
    | artifactVersion | `1.0.0`                 |
    | artifactName    | `mod_id-neoforge`       |
    | baseName        | `mod_id-neoforge-1.0.0` |

=== "mod.version = "1.0.0+some.metadata""

    | Field           | Value                          |
    |-----------------|--------------------------------|
    | modVersion      | `1.0.0`                        |
    | metadataTag     | `1.0.0+some.metadata.neoforge` |
    | uploadVersion   | `1.0.0`                        |
    | artifactVersion | `1.0.0`                        |
    | artifactName    | `mod_id-neoforge`              |
    | baseName        | `mod_id-neoforge-1.0.0`        |

=== "mod.version = "1.0.0-prerelease""

    | Field           | Value                              |
    |-----------------|------------------------------------|
    | modVersion      | `1.0.0-prerelease`                 |
    | metadataTag     | `1.0.0-prerelease+neoforge`        |
    | uploadVersion   | `1.0.0-prerelease`                 |
    | artifactVersion | `1.0.0-prerelease`                 |
    | artifactName    | `mod_id-neoforge`                  |
    | baseName        | `mod_id-neoforge-1.0.0-prerelease` |