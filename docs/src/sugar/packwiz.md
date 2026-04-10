The `com.possible-triangle.packwiz` plugin is a settings plugin.

It can be used to import [Packwiz](https://packwiz.infra.link/) packs using the modrinth or curseforge maven (1).
These dependency notations are converted into a version catelog available under the name `pack` (2).
{ .annotate }

1.  you can add them using [helper methods](/sugar/repositories)
2.  for reference, the `libs.versions.toml` version catalog has the name `libs`.

It is also possible to create multiple packwiz version catelogs from multiple packs located in different folders.
This can be useful when working with a [multi-loader](/multiloader) setup.

One caveat of being a settings plugin is that the patch version also has to be hard-coded.

![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/com.possible-triangle.packwiz?label=com.possible-triangle.packwiz&color=ff6e42&filter=1.3.*)

Presuming there is a complete packwiz pack in the default folder `pack`, including the `pack.toml` & `index.toml` file, the following describes a simple setup importing it in gradle.

```kotlin title="settings.gradle.kts"
plugins {
    id("com.possible-triangle.packwiz") version ("1.2.+")
}
```

```kotlin title="build.gradle.kts"
dependencies {
   modCompileOnly(pack.modrinth.farmers.delight)
   // or, depending on how it was added to packwiz
   modCompileOnly(pack.curseforge.farmers.delight)

   modImplementation(pack.modrinth.curios)
   
   modRuntimeOnly(pack.modrinth.jade)
}
```

## Configuration

By default, the folder `<project-dir>/pack` is used, 
but you can also overwrite this folder by modifying the pack called `"default"`

```kotlin title="settings.gradle.kts"
import com.possible_triangle.gradle.packwiz.ErrorStategy

plugins {
    id("com.possible-triangle.packwiz") version ("1.2.+")
}

packwiz {
   packs.named("default") {
      from = file("other-pack-path")
      
      curseforge = false
      modrinth = true

      // WARN will print warnings to the output
      // SKIP will ignore any error
      // FAIL (default) will abort if anything is incorrect
      strategy = ErrorStrategy.WARN
   }
}
```

## Multiple Packs

When creating a pack with a different name, the mods will be available on the `pack` version catalog with a prefix.

```kotlin title="settings.gradle.kts"
plugins {
    id("com.possible-triangle.packwiz") version ("1.2.+")
}

include("fabric", "neoforge")

packwiz {
   packs.create("neoforge") {
      from = file("neoforge/pack")
   }
   
   packs.create("fabric") {
      from = file("fabric/pack")
   }
}
```

```kotlin title="neoforge/build.gradle.kts"
dependencies {
   modRuntimeOnly(pack.neoforge.modrinth.farmers.delight)
}
```

```kotlin title="fabric/build.gradle.kts"
dependencies {
   modRuntimeOnly(pack.fabric.modrinth.farmers.delight.refabricated)
}
```