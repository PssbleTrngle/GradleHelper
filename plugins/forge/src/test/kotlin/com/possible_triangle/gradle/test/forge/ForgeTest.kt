package com.possible_triangle.gradle.test.forge

import com.modrinth.minotaur.ModrinthExtension
import com.possible_triangle.gradle.forge.ForgeExtension
import com.possible_triangle.gradle.forge.GradleHelperForgePlugin
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.test.createProject
import com.possible_triangle.gradle.test.findTestDependencies
import com.possible_triangle.gradle.test.withProjectDir
import com.possible_triangle.gradle.upload.UploadExtension
import net.minecraftforge.gradle.userdev.UserDevPlugin
import net.minecraftforge.gradle.userdev.jarjar.JarJarProjectExtension
import net.minecraftforge.gradle.userdev.tasks.JarJar
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.the
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ForgeTest {

    private fun createProjectWithForge(beforeForgeSetup: Project.() -> Unit = {}): Project {
        val project = createProject<GradleHelperForgePlugin> {
            withProjectDir("example")
        }

        project.mod {
            minecraftVersion.set("1.19.2")
        }

        project.beforeForgeSetup()

        project.configure<ForgeExtension> {
            forgeVersion.set("43.2.0")
        }

        return project
    }

    @Test
    fun `can setup forge project`() {
        val project = createProjectWithForge()

        assertNotNull(project.configurations.getByName("minecraft"))
    }

    @Test
    fun `can customize mod values after forge block`() {
        val project = createProject<GradleHelperForgePlugin> {
            withProjectDir("example")
        }

        project.configure<ForgeExtension> {
            forgeVersion.set("43.2.0")
        }

        project.mod {
            minecraftVersion.set("1.19.2")
        }
    }

    @Test
    fun `adds included libraries`() {
        val project = createProjectWithForge {
            mod {
                libraries.include("test.something:anything:1.0")
            }
        }

        val deps = project.findTestDependencies("implementation")
        assertEquals(1, deps.size)
    }

    @Test
    fun `adds included libraries added after forge setup`() {
        val project = createProjectWithForge()

        project.mod {
            libraries.include("test.something:anything:1.0")
        }

        project.dependencies {
            add("implementation", "test.something:other-thing:2.43")
        }

        val deps = project.findTestDependencies("implementation")
        assertEquals(2, deps.size)
    }

    @Test
    fun `adds included mods`() {
        val project = createProjectWithForge {
            mod {
                mods.include("test.something:anything:1.0")
            }
        }

        val deps = project.findTestDependencies("implementation")
        assertEquals(1, deps.size)
    }

    private fun Project.configureModrinth() {
        the<UploadExtension>().modrinth {
            token.set("token")
            projectId.set("id")
            changelog.set("changelog")
        }
    }

    @Test
    fun `uses jarJar file when enabled`() {
        val project = createProjectWithForge()

        project.configureModrinth()
        project.the<JarJarProjectExtension>().enable()

        project.afterEvaluate {
            val jarJarTask = project.tasks.getByName<JarJar>(UserDevPlugin.JAR_JAR_TASK_NAME)
            val file = project.the<ModrinthExtension>().file
            assertEquals(jarJarTask.archiveFile.get().asFile.path, file.get().asFile.path)
        }
    }

    @Test
    fun `uses jar file when jarjar is disabled`() {
        val project = createProjectWithForge()
        project.the<JarJarProjectExtension>().disable()

        project.configureModrinth()

        project.afterEvaluate {
            val jarTask = project.tasks.getByName<Jar>("jar")
            val file = project.the<ModrinthExtension>().file
            assertEquals(jarTask.archiveFile.get().asFile.path, file.get().asFile.path)
        }
    }

}