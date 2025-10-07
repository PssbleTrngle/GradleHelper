package com.possible_triangle.gradle.test.fabric

import com.possible_triangle.gradle.fabric.FabricExtension
import com.possible_triangle.gradle.fabric.GradleHelperFabricPlugin
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.test.createProject
import com.possible_triangle.gradle.test.withProjectDir
import org.gradle.kotlin.dsl.configure
import kotlin.test.Test
import kotlin.test.assertNotNull

class FabricTest {

    @Test
    fun `can setup fabric project`() {
        val project = createProject<GradleHelperFabricPlugin> {
            withProjectDir("example")
        }

        project.mod {
            minecraftVersion.set("1.19.2")
        }

        project.configure<FabricExtension> {
            apiVersion.set("0.76.0+1.19.2")
            loaderVersion.set("0.14.21")
        }

        assertNotNull(project.configurations.getByName("minecraft"))
    }

    @Test
    fun `can customize mod values after fabric block`() {
        val project = createProject<GradleHelperFabricPlugin> {
            withProjectDir("example")
        }

        project.configure<FabricExtension> {
            loaderVersion.set("0.14.21")
        }

        project.mod {
            minecraftVersion.set("1.19.2")
        }
    }

}