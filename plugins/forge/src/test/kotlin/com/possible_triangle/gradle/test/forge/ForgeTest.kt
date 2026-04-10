package com.possible_triangle.gradle.test.forge

import com.possible_triangle.gradle.GradleHelperCorePlugin
import com.possible_triangle.gradle.forge.GradleHelperForgePlugin
import com.possible_triangle.gradle.test.createProject
import com.possible_triangle.gradle.test.withProjectDir
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import kotlin.test.Test
import kotlin.test.assertNotNull

class ForgeTest {

    private fun createProjectWithForge(beforeForgeSetup: Project.() -> Unit = {}): Project {
        val project = createProject<GradleHelperCorePlugin> {
            withProjectDir("example-forge")
        }

        project.apply<GradleHelperForgePlugin>()

        project.beforeForgeSetup()

        return project
    }

    @Test
    fun `can setup forge project`() {
        val project = createProjectWithForge()

        assertNotNull(project.configurations.getByName("implementation"))
    }

}