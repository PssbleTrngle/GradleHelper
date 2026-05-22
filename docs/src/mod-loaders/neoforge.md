The `com.possible-triangle.neoforge` plugin is an abstract layer around NeoForge's [ModDevGradle](https://projects.neoforged.net/neoforged/ModDevGradle).

Like the other loader plugins, it tries to align with a common API interface, to make migration & sharing between them easier.

There are some neoforge-only values, which fallback to gradle properties

<div class="annotate" markdown>

| Property               | Default Value                               |
| ---------------------- | ------------------------------------------- |
| neoforgeVersion        | _gradle.properties_: `neoforge_version`     |
| kotlinForgeVersion (1) | _gradle.properties_: `kotlin_forge_version` |
| unitTests           (2) | `false`                                     |

</div>

1.  read more about [kotlin support](/kotlin)
2.  read more about [unit tests](#unit-tests)

## Access Transformers

The `neoforge` extension provides several ways to enable access transformers for the subproject.

It is also possible to use [Access Wideners](/mod-loaders/fabric#access-wideners), which will be transformed by the plugin into a access transformer (1).
{ .annotate }

1.  for debugging purposes the generated access transformer can be found under `build/generated/accesstransformer.cfg`

!!! warning

    This functionality is still a bit experimental. While it should always work, even in clean environments, sometimes the access transformer is not generated in time for the IDE sync. That means, that after running `clean`, or when cloning a fresh repository using this, you may need to sync gradle twice.

```kotlin title="build.gradle.kts"
neoforge {
   // default path at <project-path>/src/main/resources/META-INF/accesstransformer.cfg
   accessTransformer()
   accessTransformer(project(":other-sub-project"))
   // custom file path
   accessTransformer(file("..."))

   // uses `com.possible-triangle.access` to transform access widener files
   accessWidener(project(":fabric"))
}
```

These Access Transformers will also be automatically published to maven by default and can be included in another project like this:

```kotlin title="build.gradle.kts"
dependencies {

}
```

## Jar-In-Jar

Mods & Libraries [included using the mod extension](/general#including-libraries), will be bundled using neoforge's [Jar-In-Jar](https://docs.neoforged.net/toolchain/docs/dependencies/jarinjar/) system.

## Datagen

Like other loaders, it can be configured using the `dataGen` method on the `neoforge` extension.
Everything else follows the same [standard API](/general#datagen) as the other loaders.

## Unit-Tests

ModDevGradle allows [Unit Tests](https://github.com/neoforged/ModDevGradle#unit-testing-with-junit) and ships with some useful helpers.
By settings the `neoforge.unitTests` property to `true`, these test libraries are included and the test task is enabled.

An example of these Unit Tests can be found in the [Test Mod Repository](https://github.com/PssbleTrngle/TestMod/tree/main/neoforge/1.21.x/src/test/java/com/possible_triangle/test_mod/test)