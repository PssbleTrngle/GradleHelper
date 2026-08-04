# Maven

The `maven-publish` plugin is automatically applied and a maven artifact is configured.

within the `upload` extension, there are a few helper methods to configure repositories commonly used by me.

Everything is also published to `mavenLocal` by default, there is no need to do the specifically.

```kotlin title="build.gradle.kts"
upload {
   // publishes to https://registry.somethingcatchy.net
   // uses the NEXUS_USER and NEXUS_TOKEN variables
   nexus()
   // publishes to the github packages maven registry
   // uses the GITHUB_ACTOR and GITHUB_TOKEN env variables
   githubPackages()

   repositories {
      // additional maven repositories
      maven(url = uri("..."))
   }
}
```

## Artifact Modifications

There are some modifications that are applied to the artifact's metadata:

- disables [module metadata](https://docs.gradle.org/current/userguide/publishing_gradle_module_metadata.html) generation. This is an additional file that will be preferred over the POM, but is known to cause issues within mod development setups.
- _for forge projects_: remove POM dependencies completely, as they are not working transitivily at all in ForgeGradle 6
- _for non-forge projects_: remove _runtime_ dependencies. These are usually things like [JEI](https://modrinth.com/mod/jei) that are only used for testing in the dev environment and should not be included transitively by other mods. The [table below](#dependency-types) visualizes the different types of dependencies

The `com.possible-triangle.publishing` plugin, that is resposible for these modifications also exposes the helper method `MavenPublication.removePomDependencies`, which can be used to additionally exclude certain dependencies from the __POM__ by _group_, _artifact id_, _scope_ (1), or _version_
{ .annotate }

1.  like _runtime_ or _compile_

## Dependency Types

You can read about what dependency configuration to use at what time and which are represented in the __POM__ in the following table:

| Configuration     | Description                                               | Examples | Included Transitively |
| ----------------- | --------------------------------------------------------- | -------- | ------------------------- |
| modApi            | required dependencies that are needed to complile & run the mod | [Blueprint](https://modrinth.com/mod/blueprint), [Registrate](https://github.com/tterrag1098/Registrate), [Moonlight Lib](https://modrinth.com/mod/moonlight) | :fontawesome-solid-check: |
| modImplementation | optional dependencies that should be there for compile & runtime | mods to have compatiblity with | :fontawesome-solid-x:     |
| modCompileOnly    | optional dependencies that are needed only at compile time | mods to have compatiblity with | :fontawesome-solid-x:     |
| modCompileOnlyApi | required dependencies that are only needed at compile time | _no idea_ | :fontawesome-solid-check:     |
| modRuntimeOnly    | optional dependencies that are only needed at runtim | mods to have compatiblity with or help within the dev environment like [JEI](https://modrinth.com/mod/jei) | :fontawesome-solid-x:     |
