package com.possible_triangle.gradle.test.forge

import com.possible_triangle.gradle.GradleHelperCorePlugin
import com.possible_triangle.gradle.forge.ForgeExtension
import com.possible_triangle.gradle.forge.GradleHelperForgePlugin
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.test.createProject
import com.possible_triangle.gradle.test.withProjectDir
import com.possible_triangle.gradle.upload.UploadExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import kotlin.test.Test
import kotlin.test.assertNotNull

class ForgeTest {

    private fun createProjectWithForge(beforeForgeSetup: Project.() -> Unit = {}): Project {
        val project = createProject<GradleHelperCorePlugin> {
            withProjectDir("example")
        }

        project.mod {
            minecraftVersion.set("1.19.2")
        }

        project.apply<GradleHelperForgePlugin>()

        project.beforeForgeSetup()

        project.configure<ForgeExtension> {
            forgeVersion.set("43.2.0")
        }

        return project
    }

    @Test
    fun `can setup forge project`() {
        val project = createProjectWithForge()

        assertNotNull(project.configurations.getByName("implementation"))
    }

    private fun Project.configureModrinth() {
        the<UploadExtension>().modrinth {
            token.set("token")
            projectId.set("id")
            changelog.set("changelog")
        }
    }

}