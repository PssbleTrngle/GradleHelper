There are a number of default maven repositories added by the `com.possible-triangle.core` plugin.
These include the maven of Minecraft (1), SpongePowered (2) and KotlinForForge (3).
{ .annotate }

1.  https://libraries.minecraft.net/
2.  https://repo.spongepowered.org/repository/maven-public/
3.  https://thedarkcolour.github.io/KotlinForForge/

If you want to include mods from [CurseForge](https://www.cursemaven.com/) or [Modrinth](https://support.modrinth.com/en/articles/8801191-modrinth-maven), you can use these helper methods.

These are also used by the [Packwiz](/sugar/packwiz) plugin.

```kotlin title="build.gradle.kts"
repositories {
   modrinthMaven()
   curseMaven()

   // https://registry.somethingcatchy.net/#browse/browse:maven-releases
   nexus()
   // https://registry.somethingcatchy.net/#browse/browse:maven-snapshots
   nexus(snapshot = true)
}
```
