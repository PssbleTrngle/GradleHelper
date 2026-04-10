# Plugins Overview

!!! example "Experimental"
     this version is still experimental and not yet working completely!

These are a collection of gradle plugins that are meant to make minecraft mod development easier, by

1. providing sane and easy to configure defaults
2. abstracting away the individual loaders gradle plugin configuration
3. bundling helpful tools like [Minotaur](https://github.com/modrinth/minotaur "Used to publish to modrinth"), [CurseForgeGradle](https://github.com/Darkhax/CurseForgeGradle "used to publish to curseforge") or [Spotless](https://github.com/diffplug/spotless "used to format code")

It is divided into several sub-plugins, that can be used in various configurations in multi-loader projects, or simple single-loader projects.

!!! info

    You can view the [example repository](https://github.com/PssbleTrngle/TestMod) to see various different setups on the different branches. These are also automatically tested every week to verify that all plugins are still working.

You can reference any of the branches in the [example repository](https://github.com/PssbleTrngle/TestMod) or follow the steps defined in [setup](/setup).

<div class="grid cards" markdown>

-   [Core Plugin](/general)

    ***

    `com.possible-triangle.core`

    This plugin is bundled into all loader plugins. 
    For single-loader projects it does not need to be explicitly defined, only when [working with subprojects](/multiloader).
    
    It applies a lot of the overarching functionality, like configuring java & mod properties.

</div>

## Mod Loaders

In single-loader projects, on of these are the only ones that need to be applied.
For [multi-loader projects](/multiloader) projects, a combination of these can be used in various subprojects.

<div class="grid cards annotate" markdown>

-   [Common](/mod-loaders/common#common)

    ***
    
    `com.possible-triangle.common`

    using the NeoForge [ModDevGradle](https://projects.neoforged.net/neoforged/ModDevGradle) 
    and [NeoForm](https://projects.neoforged.net/neoforged/neoform)

-   [Architectury](/mod-loaders/common#architectury)

    ***
    
    `com.possible-triangle.architectury`

    using [Architectury Loom](https://docs.architectury.dev/loom/introduction) (1)
    { .annotate }

    1.  using version __1.13__

    _required if a dependendies `common` module also uses architectury loom_

-   [Vanilla](/mod-loaders/common#vanilla)

    ***
    
    `com.possible-triangle.vanilla`

    using [VanillaGradle](https://github.com/SpongePowered/VanillaGradle)

    _for common module on <=1.20.1 before NeoForge was a thing_

-   [NeoForge](/mod-loaders/neoforge)

    ***
    
    `com.possible-triangle.neoforge`
    
    using the NeoForge [ModDevGradle](https://projects.neoforged.net/neoforged/ModDevGradle)

-   [Forge](/mod-loaders/forge)

    ***
    
    `com.possible-triangle.forge`

    using [ForgeGradle](https://github.com/MinecraftForge/ForgeGradle) (1)
    { .annotate }
    
    1.  using version __7__

-   [Fabric](/mod-loaders/fabric)

    ***
    
    `com.possible-triangle.fabric`
    
    using [Fabric Loom](https://docs.fabricmc.net/develop/loom/) (1)
    { .annotate }

    1.  using version __1.15__

</div>

## Utility plugins

<div class="grid cards" markdown>

-   [Packwiz](/sugar/packwiz)

    ***
    
    `com.possible-triangle.packwiz`

    Standalone Settings Plugin.

    Allows [Packwiz](https://packwiz.infra.link/) packs to be imported as gradle dependencies.

-   Access

    ***
    
    `com.possible-triangle.access`

    Bundled within `neoforge` and `forge`, but can be used standalone.

    Transforms [Access Wideners](https://docs.fabricmc.net/develop/class-tweakers/access-widening)
    into [Access Transformers](https://docs.neoforged.net/docs/advanced/accesstransformers/)

-   [Publishing](/publishing#maven)

    ***

    `com.possible-triangle.publishing`
    
    Bundled within `core`, but can be used standalone.

    Contains helper methods to modify maven artifact metadata to work better for mod development.

</div>