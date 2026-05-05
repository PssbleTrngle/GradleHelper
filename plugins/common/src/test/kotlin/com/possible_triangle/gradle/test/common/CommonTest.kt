package com.possible_triangle.gradle.test.common

import com.possible_triangle.gradle.common.GradleHelperCommonPlugin
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.test.createProject
import com.possible_triangle.gradle.test.withProjectDir
import kotlin.test.Test

class CommonTest {
    @Test
    fun `can setup common project`() {
        val project =
            createProject<GradleHelperCommonPlugin> {
                withProjectDir("example")
            }

        project.mod {
            minecraftVersion.set("1.19.2")
        }
    }

    @Test
    fun `can customize mod values after common block`() {
        val project =
            createProject<GradleHelperCommonPlugin> {
                withProjectDir("example")
            }

        project.mod {
            minecraftVersion.set("1.19.2")
        }
    }
}
