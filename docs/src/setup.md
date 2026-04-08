## Settings Plugin 

While it is not required, the `com.possible-triangle.helper` settings plugin is recommended.

This plugin automatically adds some plugin repositories (1), for example for NeoForge, Forge, Parchment, SpongePoweded and Architectury.
{ .annotate }

1.  proxied through [registry.somethingcatchy.net](https://registry.somethingcatchy.net/#browse/browse:maven-public)

```kotlin title="settings.gradle.kts"
plugins {
    id("com.possible-triangle.helper") version ("1.2")
}
```

While all the subprojects are published using a semantic version with a patch segment, the `helper` plugin only has a major & minor versions.
All other `com.possible-triangle.*` plugins can be specified without a version, and will automatically use the `1.2.+` resolution.

This means you will get update improvements whenever a new patch is releases, but these updates should never break the existing functionality.

!!! info
    There is a [Test Mod](https://github.com/PssbleTrngle/TestMod) that creates a release by an automation twice a week to verify that any newer patch versions are still working for a variety of setups. 

    You can also reference one of it's branches as a minimal setup.

## Configure Properties

There is a variety of mod properties these plugins need in order to work.
You can find a [table of them here](general#mod-properties).

## Applying Loader Plugins

Depending on the setup, you can apply a loader plugin like `com.possible-triangle.neoforge` in the root project's `build.gradle.kts`
or only the `com.possible-triangle.core` plugin and the loader plugins in the sub-projects.

It is recommended to also referene the plugins used in sub-projects with `apply (false)`, in order to prevent class-loading issues.
You can see an example of that in the [multiloader](/multiloader) page.