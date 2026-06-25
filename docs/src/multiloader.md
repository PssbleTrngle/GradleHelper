# Multi-Loader

## Choosing the common plugin

While these plugins are completely fine to work in simple, single-loader projects,
they are really design to improve multi-loader setups using multiple sub-projects without having to add
the same tons of boilerplate code everywhere.

Since there are multiple plugins serving the function of a _common_ sub-project,
here is a table of when it makes sense to use which one.

| Plugin                               | When to use                                                                 |
| ------------------------------------ | --------------------------------------------------------------------------- |
| `com.possible-triangle.common`       | Starting with 1.21.1                                                        |
| `com.possible-triangle.vanilla`      | Minecraft 1.20.1 and below                                                  |

## Cross-Module Dependencies

Dependencies between the different sub-projects can be defined using the `dependsOn` method withing the loader's extension block.
These will already add source sets & merge outputs in the final JAR's, no extra configuration needed.

## Examples

Multiple examples can be found in the [example repository](https://github.com/PssbleTrngle/TestMod).
Below is a basic example of a multi-loader project on minecraft 1.21.1 and above.

```kotlin title="build.gradle.kts"
plugins {
    id("com.possible-triangle.core")
    id("com.possible-triangle.common") apply false
    id("com.possible-triangle.forge") apply false
    id("com.possible-triangle.fabric") apply false
}

subprojects {
   // only required if you want to already configure something
   // plugin related withing this "subprojects" block
    apply(plugin = "com.possible-triangle.core")

    upload {
      maven {
         githubPackages()
      }
    }
}
```

```kotlin title="common/build.gradle.kts"
plugins {
    id("com.possible-triangle.common")
}
```

```kotlin title="neoforge/build.gradle.kts"
plugins {
    id("com.possible-triangle.neoforge")
}

neoforge {
    dependOn(project(":common"))
}
```

```kotlin title="fabric/build.gradle.kts"
plugins {
    id("com.possible-triangle.fabric")
}

fabric {
    dependOn(project(":common"))
}
```

## Do's and Don'ts

```kotlin title="common/build.gradle.kts"
plugins {
    id("com.possible-triangle.common")
}

dependencies {
    // 🙅‍♂️ Don't: will provide the common module to the fabric/neoforge projects, which cannot handle it
    modApi(libs.moonlight.common)

    // 👌 Do: will only add it to the common module
    modImplementation(libs.moonlight.common)
}
```

```kotlin title="neoforge/build.gradle.kts"
plugins {
    id("com.possible-triangle.neoforge")
}

neoforge {
    dependOn(project(":common"))

    // 🙅‍♂️ Don't: the mods common module is marked as "common" and cannot be consumed by neoforge
    modApi(libs.moonlight.common)

    // 👌 Do: the mods loader-specific JAR also includes the common source code
    // and is marked as "neoforge"
    modApi(libs.moonlight.neoforge)
}
```

```kotlin title="fabric/build.gradle.kts"
plugins {
    id("com.possible-triangle.fabric")
}

fabric {
    dependOn(project(":common"))

    // 🙅‍♂️ Don't: the mods common module is marked as "common" and cannot be consumed by neoforge
    modApi(libs.moonlight.common)

    // 👌 Do: the mods loader-specific JAR also includes the common source code
    // and is marked as "fabric"
    modApi(libs.moonlight.fabric)
}
```