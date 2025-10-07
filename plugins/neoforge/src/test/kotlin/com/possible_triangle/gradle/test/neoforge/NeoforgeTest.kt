package com.possible_triangle.gradle.test.neoforge

import com.modrinth.minotaur.ModrinthExtension
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.neoforge.GradleHelperNeoForgePlugin
import com.possible_triangle.gradle.neoforge.NeoforgeExtension
import com.possible_triangle.gradle.test.createProject
import com.possible_triangle.gradle.test.findTestDependencies
import com.possible_triangle.gradle.test.withProjectDir
import com.possible_triangle.gradle.upload.UploadExtension
import net.neoforged.gradle.dsl.common.extensions.JarJar
import net.neoforged.gradle.userdev.UserDevProjectPlugin
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.the
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import net.neoforged.gradle.common.tasks.JarJar as JarJarTask

class NeoforgeTest {

    private fun createProjectWithNeoforge(beforeNeoforgeSetup: Project.() -> Unit = {}): Project {
        val project = createProject<GradleHelperNeoForgePlugin> {
            withProjectDir("example")
        }

        project.mod {
            minecraftVersion.set("1.21")
        }

        project.beforeNeoforgeSetup()

        project.configure<NeoforgeExtension> {
            neoforgeVersion.set("21.0.54-beta")
        }

        return project
    }

    @Test
    fun `can setup neoforge project`() {
        val project = createProjectWithNeoforge()

        assertNotNull(project.configurations.getByName("implementation"))
    }

    @Test
    fun `can customize mod values after neoforge block`() {
        val project = createProject<GradleHelperNeoForgePlugin> {
            withProjectDir("example")
        }

        project.configure<NeoforgeExtension> {
            neoforgeVersion.set("21.0.54-beta")
        }

        project.mod {
            minecraftVersion.set("1.21")
        }
    }

    @Test
    fun `adds included libraries`() {
        val project = createProjectWithNeoforge {
            mod {
                libraries.include("test.something:anything:1.0")
            }
        }

        val deps = project.findTestDependencies("implementation")
        assertEquals(1, deps.size)
    }

    @Test
    fun `adds included libraries added after neoforge setup`() {
        val project = createProjectWithNeoforge()

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
        val project = createProjectWithNeoforge {
            mod {
                libraries.include("test.something:anything:1.0")
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
        val project = createProjectWithNeoforge()

        project.configureModrinth()
        project.the<JarJar>().enable()

        project.afterEvaluate {
            val jarJarTask = project.tasks.getByName<JarJarTask>(UserDevProjectPlugin.JAR_JAR_TASK_NAME)
            val file = project.the<ModrinthExtension>().file
            assertEquals(jarJarTask.archiveFile.get().asFile.path, file.get().asFile.path)
        }
    }

    @Test
    fun `uses jar file when jarjar is disabled`() {
        val project = createProjectWithNeoforge()
        project.the<JarJar>().disable()

        project.configureModrinth()

        project.afterEvaluate {
            val jarTask = project.tasks.getByName<Jar>("jar")
            val file = project.the<ModrinthExtension>().file
            assertEquals(jarTask.archiveFile.get().asFile.path, file.get().asFile.path)
        }
    }

}