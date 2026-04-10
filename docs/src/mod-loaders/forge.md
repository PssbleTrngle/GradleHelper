The `com.possible-triangle.forge` plugin is an abstract layer around [ForgeGradle](https://github.com/MinecraftForge/ForgeGradle), specifically version 7.

Like the other loader plugins, it tries to align with a common API interface, to make migration & sharing between them easier.

There are some forge-only values, which fallback to gradle properties

<div class="annotate" markdown>

| Property               | Default Value                               |
| ---------------------- | ------------------------------------------- |
| mappingChannel         | `"official"`                                |
| mappingVersion         | `mod.minecraftVersion`                      |
| forgeVersion           | _gradle.properties_: `forge_version`        |
| kotlinForgeVersion (1) | _gradle.properties_: `kotlin_forge_version` |

</div>

1.  read more about [kotlin support](/kotlin)

## Datagen

Like other loaders, it can be configured using the `dataGen` method on the `forge` extension.
Everything else follows the same [standard API](/general#datagen) as the other loaders.
