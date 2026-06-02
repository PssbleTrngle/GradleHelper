package com.possible_triangle.gradle

import com.possible_triangle.gradle.repositories.defaultRepositories
import com.possible_triangle.gradle.upload.setupUpload
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.withType
import kotlin.text.get

class GradleHelperCorePlugin : Plugin<Project> {
    override fun apply(target: Project) = target.configure()

    private fun Project.configure() {
        loadEnv()

        logger.lifecycle("applying gradle helper ${BuildParameters.PLUGIN_VERSION} in ${project.name}")

        val rootMod = coreProject.takeUnless { it == this }?.extensions?.findByType<ModExtension>()
        val mod = extensions.create<ModExtension, ModExtensionImpl>("mod")

        fun <T : Any> configureDefault(
            default: Provider<T>,
            supplier: ModExtension.() -> Property<T>,
        ) {
            mod.supplier().convention(provider { rootMod?.supplier()?.orNull ?: default.get() })
        }

        val rawModVersion = env.provider("RELEASE_VERSION").orElse(providers.gradleProperty("mod_version"))
        val patchVersion = env.provider("PATCH_VERSION").orElse("999")
        val modVersion =
            rawModVersion.map {
                it
                    .replace("<patch>", patchVersion.get())
                    .replace("<mc>", mod.minecraftVersion.get())
            }

        val mcVersion =
            coreProject.providers
                .gradleProperty("mc_version")
                .orElse(coreProject.providers.gradleProperty("minecraft_version"))

        configureDefault(providers.gradleProperty("mod_id")) { id }
        configureDefault(providers.gradleProperty("mod_name")) { name }
        configureDefault(modVersion) { version }
        configureDefault(providers.gradleProperty("mod_author")) { author }
        configureDefault(providers.gradleProperty("mod_description")) { description }
        configureDefault(mcVersion) { minecraftVersion }
        configureDefault(providers.gradleProperty("release_type").orElse("release")) { releaseType }
        configureDefault(providers.gradleProperty("repository")) { repository }
        configureDefault(providers.gradleProperty("maven_group")) { mavenGroup }

        repositories {
            defaultRepositories()
        }

        setupJava()
        configureBaseName()
        setupUpload()
        setupReleaseMetadata()

        tasks.withType<Jar> {
            exclude(".cache")
            exclude("**/*.bbmodel")
            exclude("**/*.aseprite")
            exclude("**/*.xcf")
        }

        // needed so compile-only dependencies are also available for tests
        configurations.named("testCompileOnly") {
            extendsFrom(configurations.getByName("compileOnly"))
        }

        tasks.withType<Test> {
            exclude("**/mixins/**")
            exclude("**/mixin/**")
        }
    }
}
