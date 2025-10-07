package com.possible_triangle.gradle.test

import com.possible_triangle.gradle.GradleHelperCorePlugin
import com.possible_triangle.gradle.mod
import kotlin.test.Test
import kotlin.test.assertEquals

class SubprojectTest {

    @Test
    fun `mod properties are accessible from sub projects`() {
        val project = createProject<GradleHelperCorePlugin> {
            withName("parent")
            withProjectDir("example")
        }

        val subproject = createProject<GradleHelperCorePlugin> {
            withName("project-1")
            withParent(project)
        }

        val otherSubproject = createProject<GradleHelperCorePlugin> {
            withName("project-2")
            withParent(project)
        }

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
        val project = createProject<GradleHelperCorePlugin> {
            withName("parent")
            withProjectDir("example")
        }

        val subproject = createProject<GradleHelperCorePlugin> {
            withName("project-1")
            withParent(project)
        }

        val otherSubproject = createProject<GradleHelperCorePlugin> {
            withName("project-2")
            withParent(project)
        }

        subproject.mod {
            name.set("Subproject Mod Name")
        }

        assertEquals("example-mod", subproject.mod.id.get())
        assertEquals("Subproject Mod Name", subproject.mod.name.get())
        assertEquals("Example Name", otherSubproject.mod.name.get())
    }

}