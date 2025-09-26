package com.possible_triangle.gradle.test

import com.modrinth.minotaur.ModrinthExtension
import com.possible_triangle.gradle.features.loaders.ModLoader
import com.possible_triangle.gradle.features.loaders.detectModLoader
import com.possible_triangle.gradle.mod
import net.neoforged.gradle.dsl.common.extensions.JarJar
import net.neoforged.gradle.userdev.UserDevProjectPlugin
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import net.neoforged.gradle.common.tasks.JarJar as JarJarTask

class NeoforgeTest {

    private fun createProjectWithNeoforge(beforeNeoforgeSetup: Project.() -> Unit = {}): Project {
        val project = createProject {
            withProjectDir("example")
        }

        project.mod {
            minecraftVersion.set("1.21")
        }

        project.beforeNeoforgeSetup()

        project.neoforge {
            neoforgeVersion = "21.0.54-beta"
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
        val project = createProject {
            withProjectDir("example")
        }

        project.neoforge {
            neoforgeVersion = "21.0.54-beta"
        }

        project.mod {
            minecraftVersion.set("1.21")
        }
    }

    @Test
    fun `detects neoforge modloader`() {
        val project = createProjectWithNeoforge()

        assertEquals(ModLoader.NEOFORGE, project.detectModLoader())
    }

    @Test
    fun `adds included libraries`() {
        val project = createProjectWithNeoforge {
            mod {
                includedLibraries.set(listOf("test.something:anything:1.0"))
            }
        }

        val deps = project.findTestDependencies("implementation")
        assertEquals(1, deps.size)
    }

    @Test
    fun `adds included libraries added after neoforge setup`() {
        val project = createProjectWithNeoforge()

        project.mod {
            includedLibraries.set(listOf("test.something:anything:1.0"))
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
                includedMods.set(listOf("test.something:anything:1.0"))
            }
        }

        val deps = project.findTestDependencies("implementation")
        assertEquals(1, deps.size)
    }

    private fun Project.configureModrinth() {
        uploadToModrinth {
            token = "token"
            projectId = "id"
            changelog = "changelog"
        }
    }

    @Test
    fun `uses jarJar file when enabled`() {
        val project = createProjectWithNeoforge()

        project.configureModrinth()
        project.the<JarJar>().enable()

        val jarJarTask = project.tasks.getByName<JarJarTask>(UserDevProjectPlugin.JAR_JAR_TASK_NAME)
        val file = project.the<ModrinthExtension>().file
        assertEquals(jarJarTask.archiveFile.get().asFile.path, file.get().asFile.path)
    }

    @Test
    fun `uses jar file when jarjar is disabled`() {
        val project = createProjectWithNeoforge()
        project.the<JarJar>().disable()

        project.configureModrinth()

        val jarTask = project.tasks.getByName<Jar>("jar")
        val file = project.the<ModrinthExtension>().file
        assertEquals(jarTask.archiveFile.get().asFile.path, file.get().asFile.path)
    }

}