package com.possible_triangle.gradle.test.common

import com.possible_triangle.gradle.GradleHelperCorePlugin
import com.possible_triangle.gradle.common.GradleHelperCommonPlugin
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.test.createProject
import com.possible_triangle.gradle.test.findTestDependencies
import com.possible_triangle.gradle.test.withProjectDir
import kotlin.test.Test
import kotlin.test.assertEquals

class CommonTest {

    @Test
    fun `can setup common project`() {
        val project = createProject<GradleHelperCommonPlugin> {
            withProjectDir("example")
        }

        project.mod {
            minecraftVersion.set("1.19.2")
        }
    }

    @Test
    fun `can customize mod values after common block`() {
        val project = createProject<GradleHelperCommonPlugin> {
            withProjectDir("example")
        }

        project.mod {
            minecraftVersion.set("1.19.2")
        }
    }

    @Test
    fun `applies all included libraries`() {
        val project = createProject<GradleHelperCorePlugin> {
            withProjectDir("example")
        }

        val subproject = createProject<GradleHelperCommonPlugin> {
            withParent(project)
        }

        project.mod {
            libraries.include("test.something:anything:1.0")
            libraries.include("test.something:else:1.0")
        }

        assertEquals(2, project.mod.libraries.get().size)
        assertEquals(2, subproject.mod.libraries.get().size)

        subproject.mod {
            libraries.include("test.something:that:1.0")
        }

        val deps = subproject.findTestDependencies("implementation")
        assertEquals(3, deps.size)
    }

}