By calling `enableSpotless()` in the root `build.gradle.kts` file, the [Spotless Plugin](https://github.com/diffplug/spotless) will be enabled for all projects.

It is used to format code, including JSON files withing the _main_ source set.

There are a few rules automatically configured and applied, but in general it is at a rather minimal configuration by default.