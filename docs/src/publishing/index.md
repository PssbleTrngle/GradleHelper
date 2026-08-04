# Publishing

This plugin makes publishig mods to [maven](maven) as well as [curseforge & modrinth](upload) easier.
Specifics about how files & versions are named can be configured using a [VersionStrategy](version-strategy).

Everything is combined in a `upload` gradle task, that will also generate a `build/release.json`, which can be used by [@PssbleTrngle Workflows](https://workflows.somethingcatchy.net/docs/releases/notifications/) to send release notifications, or to create git tags from the created version.