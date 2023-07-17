package com.possible_triangle.gradle.test

import com.possible_triangle.gradle.features.loaders.ModLoader
import com.possible_triangle.gradle.features.loaders.detectModLoader
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.forge
import org.gradle.kotlin.dsl.mod
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ForgeTest {

    private fun createProjectWithForge(beforeForgeSetup: Project.() -> Unit = {}): Project {
        val project = createProject {
            withProjectDir("example")
        }

        project.mod {
            minecraftVersion.set("1.19.2")
        }

        project.beforeForgeSetup()

        project.forge {
            forgeVersion = "43.2.0"
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
        val project = createProject {
            withProjectDir("example")
        }

        project.forge {
            forgeVersion = "43.2.0"
        }

        project.mod {
            minecraftVersion.set("1.19.2")
        }
    }

    @Test
    fun `detects forge modloader`() {
        val project = createProjectWithForge()

        assertEquals(ModLoader.FORGE, project.detectModLoader())
    }

    @Test
    fun `adds included libraries`() {
        val project = createProjectWithForge {
            mod {
                includedLibraries.set(listOf("test.something:anything:1.0"))
            }
        }

        val deps = project.findTestDependencies("implementation")
        assertEquals(1, deps.size)
    }

    @Test
    fun `adds included libraries added after forge setup`() {
        val project = createProjectWithForge()

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
        val project = createProjectWithForge {
            mod {
                includedMods.set(listOf("test.something:anything:1.0"))
            }
        }

        val deps = project.findTestDependencies("implementation")
        assertEquals(1, deps.size)
    }

}