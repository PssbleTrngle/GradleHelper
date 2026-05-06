package com.possible_triangle.gradle

import com.possible_triangle.gradle.repositories.defaultRepositories
import com.possible_triangle.gradle.upload.configureUpload
import com.possible_triangle.gradle.upload.registerUpload
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.withType

class GradleHelperCorePlugin : Plugin<Project> {
    override fun apply(target: Project) = target.configure()

    private fun Project.configure() {
        loadEnv()

        logger.info("applying gradle helper plugin with version ${BuildParameters.PLUGIN_VERSION}")

        val rootMod = coreProject.takeUnless { it == this }?.extensions?.findByType<ModExtension>()
        val mod = extensions.create<ModExtension, ModExtensionImpl>("mod")

        fun <T : Any> configureDefault(
            default: T?,
            supplier: ModExtension.() -> Property<T>,
        ) {
            mod.supplier().convention(provider { rootMod?.supplier()?.orNull ?: default })
        }

        val modVersion = env["RELEASE_VERSION"] ?: coreProject.stringProperty("mod_version")
        val mcVersion = coreProject.stringProperty("mc_version") ?: coreProject.stringProperty("minecraft_version")

        configureDefault(coreProject.stringProperty("mod_id")) { id }
        configureDefault(coreProject.stringProperty("mod_name")) { name }
        configureDefault(modVersion) { version }
        configureDefault(coreProject.stringProperty("mod_author")) { author }
        configureDefault(coreProject.stringProperty("mod_description")) { description }
        configureDefault(mcVersion) { minecraftVersion }
        configureDefault(coreProject.stringProperty("release_type")) { releaseType }
        configureDefault(coreProject.stringProperty("repository")) { repository }
        configureDefault(coreProject.stringProperty("maven_group")) { mavenGroup }

        repositories {
            defaultRepositories()
        }

        registerUpload()
        setupJava()
        configureBaseName()
        setupReleaseMetadata()

        tasks.withType<Jar> {
            exclude(".cache")
            exclude("**/*.bbmodel")
            exclude("**/*.aseprite")
            exclude("**/*.xcf")
        }

        if (subprojects.isEmpty()) {
            setupSubprojectOnly()
        }

        // needed so compile-only dependencies are also available for tests
        configurations.named("testCompileOnly") {
            extendsFrom(configurations.getByName("compileOnly"))
        }

        tasks.withType<Test> {
            exclude("**/mixins/**")
            exclude("**/mixin/**")
        }

        // disable tests, these sometimes break builds because no test sources are found
        // tasks.withType<Test> { enabled = false }
        // tasks.named("compileTestJava") { enabled = false }
        // tasks.findByName("compileTestKotlin")?.enabled = false
    }

    private fun Project.setupSubprojectOnly() {
        createReleaseMetadata()
        configureUpload()
    }
}
