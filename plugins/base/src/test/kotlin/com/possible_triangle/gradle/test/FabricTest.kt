package com.possible_triangle.gradle.test

import com.possible_triangle.gradle.features.loaders.ModLoader
import com.possible_triangle.gradle.features.loaders.detectModLoader
import org.gradle.kotlin.dsl.fabric
import com.possible_triangle.gradle.mod
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FabricTest {

    @Test
    fun `can setup fabric project`() {
        val project = createProject {
            withProjectDir("example")
        }

        project.mod {
            minecraftVersion.set("1.19.2")
        }

        project.fabric {
            apiVersion = "0.76.0+1.19.2"
            loaderVersion = "0.14.21"
        }

        assertNotNull(project.configurations.getByName("minecraft"))
    }

    @Test
    fun `can customize mod values after fabric block`() {
        val project = createProject {
            withProjectDir("example")
        }

        project.fabric {
            loaderVersion = "0.14.21"
        }

        project.mod {
            minecraftVersion.set("1.19.2")
        }
    }

    @Test
    fun `detects forge modloader`() {
        val project = createProject {
            withProjectDir("example")
        }

        project.mod {
            minecraftVersion.set("1.19.2")
        }

        project.fabric {
            loaderVersion = "0.14.21"
        }

        assertEquals(ModLoader.FABRIC, project.detectModLoader())
    }

}