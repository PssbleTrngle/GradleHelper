The `com.possible-triangle.forge` plugin is an abstract layer around [ForgeGradle](https://github.com/MinecraftForge/ForgeGradle), specifically version 6.

Like the other loader plugins, it tries to align with a common API interface, to make migration & sharing between them easier.

There are some forge-only values, which fallback to gradle properties

<div class="annotate" markdown>

| Property               | Default Value                            |
| ---------------------- | ---------------------------------------- |
| mappingChannel         | `"official"`                             |
| mappingVersion         | `mod.minecraftVersion`                   |
| forgeVersion           | _gradle.properties_: `forge_version`        |
| kotlinForgeVersion (1) | _gradle.properties_: `kotlin_forge_version` |

</div>

1.  read more about [kotlin support](kotlin)

## Access Tranformers

The `forge` extension provides several ways to enable access transformers for the subproject.

It is also possible to use [Access Wideners](fabric#access-wideners), which will be transformed by the plugin into a remapped access transformer (1).
{ .annotate }

1.  for debugging purposes the generated access transformer can be found under `build/generated/accesstransformer.cfg`

!!! warning

    This functionality is still a bit experimental. While it should always work, even in clean environments, sometimes the access transformer is not generated in time for the IDE sync. That means, that after running `clean`, or when cloning a fresh repository using this, you may need to sync gradle twice.

```kotlin
forge {
   // default path at <project-path>/src/main/resources/META-INF/accesstransformer.cfg
   accessTransformer()
   accessTransformer(project(":other-sub-project"))
   // custom file path
   accessTransformer(file("..."))

   // uses `com.possible-triangle.access` to transform access widener files
   accessWidener(project(":fabric"))
}
```

## Jar-In-Jar

Mods & Libraries [included using the mod extension](core#including-libraries), will be bundled using forge's [Jar-In-Jar](https://docs.minecraftforge.net/en/fg-6.x/dependencies/jarinjar/) system.

The `jarJar` extension will automatically be enabled if any mods or libraries are marked as being included.

It will result in two _JAR_ files being created, one with everything bundled in, and one without (1).
{ .annotate }

1.  This jar file will get the `:slim` classifier and will also be published to maven

Uploading to modrinth or curseforge will automatically choose the _jarJar_ artifact, if any is created.

## Mixins

Forge is the only loader where mixin support needs to be enabled specifically.

This will automatically include the neccessary compile & runtime dependencies,
including and bundling [Mixin Extras](https://github.com/LlamaLad7/MixinExtras).

```kotlin
forge {
   enableMixins()
}
```

## Datagen

Like other loaders, it can be configured using the `dataGen` method on the `forge` extension.
Everything else follows the same [standard API](core#datagen) as the other loaders.
