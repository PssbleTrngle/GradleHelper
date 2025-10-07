package com.possible_triangle.gradle.test

import com.possible_triangle.gradle.GradleHelperCorePlugin
import com.possible_triangle.gradle.features.detectKotlin
import com.possible_triangle.gradle.features.enableKotlin
import kotlin.test.Test
import kotlin.test.assertTrue

class KotlinTest {

    @Test
    fun `detects kotlin`() {
        val project = createProject<GradleHelperCorePlugin> {
            withProjectDir("example")
        }

        project.enableKotlin()

        assertTrue(project.detectKotlin())
    }

}