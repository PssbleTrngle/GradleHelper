package com.possible_triangle.gradle.test

import com.possible_triangle.gradle.GradleHelperPlugin
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.common
import org.gradle.kotlin.dsl.mod
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SubprojectTest {

    @Test
    fun `mod properties are accessible from sub projects`() {
        val project = createProjectWithoutPlugin {
            withName("parent")
            withProjectDir("example")
        }

        val subproject = createProjectWithoutPlugin {
            withName("project-1")
            withParent(project)
        }

        val otherSubproject = createProjectWithoutPlugin {
            withName("project-2")
            withParent(project)
        }

        project.apply<GradleHelperPlugin>()

        project.mod {
            name.set("Another Name")
        }

        assertEquals("example-mod", subproject.mod.id.get())
        assertEquals("example-mod", otherSubproject.mod.id.get())

        assertEquals("Another Name", subproject.mod.name.get())
        assertEquals("Another Name", otherSubproject.mod.name.get())
    }

    @Test
    fun `mod properties can be overwritten in sub projects`() {
        val project = createProjectWithoutPlugin {
            withName("parent")
            withProjectDir("example")
        }

        val subproject = createProjectWithoutPlugin {
            withName("project-1")
            withParent(project)
        }

        val otherSubproject = createProjectWithoutPlugin {
            withName("project-2")
            withParent(project)
        }

        project.apply<GradleHelperPlugin>()

        subproject.mod {
            name.set("Subproject Mod Name")
        }

        assertEquals("example-mod", subproject.mod.id.get())
        assertEquals("Subproject Mod Name", subproject.mod.name.get())
        assertEquals("Example Name", otherSubproject.mod.name.get())
    }

    @Test
    fun `applies all included libraries`() {
        val project = createProjectWithoutPlugin {
            withProjectDir("example")
        }

        val subproject = createProjectWithoutPlugin {
            withParent(project)
        }

        project.apply<GradleHelperPlugin>()

        project.mod {
            includedLibraries.add("test.something:anything:1.0")
            includedLibraries.add("test.something:else:1.0")
        }

        assertEquals(2, project.mod.includedLibraries.get().size)
        assertEquals(2, subproject.mod.includedLibraries.get().size)

        subproject.common {
            includesLibrary("test.something:that:1.0")
        }

        val deps = subproject.findTestDependencies("implementation")
        assertEquals(3, deps.size)
    }

}