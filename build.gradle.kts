import groovy.util.Node
import groovy.util.NodeList
import org.gradle.internal.impldep.org.joda.time.LocalDateTime

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
        maven { url = uri("https://maven.minecraftforge.net") }
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")

        classpath("org.spongepowered:vanillagradle:0.2.1-SNAPSHOT")
        classpath("net.minecraftforge.gradle:ForgeGradle:[6.0,6.2)")
        classpath("fabric-loom:fabric-loom.gradle.plugin:1.2-SNAPSHOT")

        classpath("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:4.2.1.3168")

        classpath("net.darkhax.curseforgegradle:CurseForgeGradle:1.1.15")
        classpath("com.modrinth.minotaur:Minotaur:2.+")
    }
}

fun Project.java(block: JavaPluginExtension.() -> Unit) =
    extensions.getByType<JavaPluginExtension>().apply(block)

fun Project.publishing(block: PublishingExtension.() -> Unit) =
    extensions.getByType<PublishingExtension>().apply(block)

class ProjectEnvironment(private val values: Map<String, String>) {
    operator fun get(key: String) = values[key]
    val isCI get() = get("CI") == "true"
}

fun Project.loadEnv(fileName: String = ".env"): ProjectEnvironment {
    val localEnv = file(fileName).takeIf { it.exists() }?.readLines()?.associate {
        val (key, value) = it.split("=")
        key.trim() to value.trim()
    } ?: emptyMap()

    return ProjectEnvironment(System.getenv() + localEnv)
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    val env = loadEnv()

    val mod_id: String by extra
    val mod_name: String by extra
    val mod_author: String by extra
    val mod_version: String by extra
    val repository: String by extra
    val artifact_group: String by extra

    ext["enableKotlin"] = {
        apply(plugin = "org.jetbrains.kotlin.jvm")
    }

    ext["env"] = { loadEnv() }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
        withSourcesJar()
    }

    tasks.withType<Jar> {
        val now = LocalDateTime.now().toString()

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${mod_name}" }
        }

        manifest {
            attributes(
                mapOf(
                    "Specification-Title" to mod_name,
                    "Specification-Vendor" to mod_author,
                    "Specification-Version" to mod_version,
                    "Implementation-Title" to name,
                    "Implementation-Version" to archiveVersion,
                    "Implementation-Vendor" to mod_author,
                    "Implementation-Timestamp" to now,
                )
            )
        }
    }

    tasks.named<Jar>("sourcesJar") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${mod_name}" }
        }
    }

    // Disables Gradle's custom module metadata from being published to maven. The
    // metadata includes mapped dependencies which are not reasonably consumable by
    // other mod developers.
    tasks.withType<GenerateModuleMetadata> {
        enabled = false
    }

    repositories {
        maven {
            url = uri("https://repo.spongepowered.org/repository/maven-public/")
            content {
                includeGroup("org.spongepowered")
            }
        }

        maven {
            url = uri("https://api.modrinth.com/maven")
            content {
                includeGroup("maven.modrinth")
            }
        }

        maven {
            url = uri("https://www.cursemaven.com")
            content {
                includeGroup("curse.maven")
            }
        }

        maven {
            url = uri("https://thedarkcolour.github.io/KotlinForForge/")
            content {
                includeGroup("thedarkcolour")
            }
        }
    }

    @Suppress("UnstableApiUsage")
    tasks.withType<ProcessResources> {
        // this will ensure that this task is redone when the versions change.
        inputs.property("version", version)

        filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta", "fabric.mod.json", "${mod_id}.mixins.json")) {
            expand(
                mapOf(
                    "version" to mod_version,
                    "mod_name" to mod_name,
                    "mod_id" to mod_id,
                    "mod_author" to mod_author,
                    "repository" to repository,
                )
            )
        }
    }

    ext["enablePublishing"] = { id: String ->
        publishing {
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/${repository}")
                    version = mod_version
                    credentials {
                        username = env["GITHUB_ACTOR"]
                        password = env["GITHUB_TOKEN"]
                    }
                }
            }
            publications {
                create<MavenPublication>("gpr") {
                    groupId = artifact_group
                    artifactId = "${mod_id}-${project.name}"
                    version = mod_version
                    from(components["java"])

                    pom.withXml {
                        val node = asNode()
                        val list = node.get("dependencies") as NodeList
                        list.forEach { node.remove(it as Node) }
                    }
                }
            }
        }
    }
}