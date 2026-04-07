There are several plugins that can be used for a _common_ sub-project to be shared between multiple mod-loaders.
All of these provide a mod-loader-agnosistic source set and provide a `common` extension to configure.

Which can and should be choosen when is listed on the [multiloader](/multiloader#choosing-the-common-plugin) page.

## NeoForm

The `com.possible-triangle.common` uses [NeoForm](https://projects.neoforged.net/neoforged/neoform)
through [ModDevGradle](https://projects.neoforged.net/neoforged/ModDevGradle).

There is one loader-specific property on the `common` extension, which can but should never have to be overwritten.

| Property       | Default Value                            |
| -------------- | ---------------------------------------- |
| neoformVersion | _fetched using the mod.minecraftVersion_ |

The `common` extension implements all `accessTransformer` methods that are present in the [neoforge plugin](/mod-loaders/neoforge),
as well as all `accessWidener` methods that are present in the [fabric plugin](/mod-loaders/fabric).
The latter will be transformed into an access transformer, as [described in the neoforge page](/mod-loaders/neoforge#access-transformers)

## Architectury

The `com.possible-triangle.architectury` uses [Architectury Loom](https://docs.architectury.dev/loom/introduction), specifically version 1.13.

The `common` extension implements all `accessWidener` methods that are present in the [fabric plugin](/mod-loaders/fabric).

## Vanilla

The `com.possible-triangle.vanilla` uses [VanillaGradle](https://github.com/SpongePowered/VanillaGradle)
