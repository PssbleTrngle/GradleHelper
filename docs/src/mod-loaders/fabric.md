The `com.possible-triangle.fabric` plugin is an abstract layer around [Fabric Loom](https://docs.fabricmc.net/develop/loom/), specifically version 1.11.

Like the other loader plugins, it tries to align with a common API interface, to make migration & sharing between them easier.

There are some fabric-only values, which fallback to gradle properties

<div class="annotate" markdown>

| Property                | Default Value                                |
| ----------------------- | -------------------------------------------- |
| loaderVersion           | _gradle.properties_: `fabric_loader_version` |
| apiVersion              | _gradle.properties_: `fabric_api_version`    |
| kotlinFabricVersion (1) | _gradle.properties_: `kotlin_fabric_version` |

</div>

1.  read more about [kotlin support](/kotlin)

## Access Wideners

The `fabric` extension provides several ways to enable access wideners for the subproject.

```kotlin title="build.gradle.kts"
neoforge {
   // default path at <project-path>/src/main/resources/<mod-id>.accesswidener
   accessWidener()
   accessWidener(project(":other-sub-project"))
   // custom file path
   accessWidener(file("..."))
}
```


## Datagen

Like other loaders, it can be configured using the `dataGen` method on the `fabric` extension.
Everything else follows the same [standard API](general#datagen) as the other loaders.

Existing mods are also added as system properties to allow usage of [Porting Lib's](https://github.com/Fabricators-of-Create/Porting-Lib) `ExistingFileHelper`.
