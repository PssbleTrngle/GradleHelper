package com.possible_triangle.gradle.test

import org.gradle.kotlin.dsl.common
import org.gradle.kotlin.dsl.mod
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class CommonTest {

    @Test
    fun `can setup common project`() {
        val project = createProject {
            withProjectDir("example")
        }

        project.mod {
            minecraftVersion.set("1.19.2")
        }

        project.common()

        assertNotNull(project.configurations.getByName("minecraft"))
    }

    @Test
    fun `can customize mod values after common block`() {
        val project = createProject {
            withProjectDir("example")
        }

        project.common()
        project.mod {
            minecraftVersion.set("1.19.2")
        }
    }

}