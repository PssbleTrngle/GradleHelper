The `com.possible-triangle.forge` plugin is an abstract layer around NeoForge's [Legacy Forge Plugin](https://github.com/neoforged/ModDevGradle/blob/main/LEGACY.md).

Like the other loader plugins, it tries to align with a common API interface, to make migration & sharing between them easier.

There is one forge-only values, which fallback to a gradle property

<div class="annotate" markdown>

| Property               | Default Value                               |
| ---------------------- | ------------------------------------------- |
| kotlinForgeVersion (1) | _gradle.properties_: `kotlin_forge_version` |

</div>

1.  read more about [kotlin support](/kotlin)

## Datagen

Like other loaders, it can be configured using the `dataGen` method on the `forge` extension.
Everything else follows the same [standard API](/general#datagen) as the other loaders.
