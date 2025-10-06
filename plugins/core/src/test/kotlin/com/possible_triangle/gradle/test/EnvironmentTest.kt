package com.possible_triangle.gradle.test

import com.possible_triangle.gradle.GradleHelperCorePlugin
import com.possible_triangle.gradle.env
import org.gradle.kotlin.dsl.apply
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class EnvironmentTest {

    private val localEnv = mapOf(
        "A_TOKEN" to "************",
        "SOMETHING_ELSE" to "test",
        "THE_THING" to "OTHER",
        "WITH_SPACE" to "VALUE",
        "CHANGELOG" to "Something Something",
    )

    @Test
    fun `loads load environment files`() {
        createProject<GradleHelperCorePlugin> {
            withProjectDir("with-env")
        }

        localEnv.forEach {(key, value) ->
            assertContains(env.toMap(), key)
            assertEquals(env[key], value)
        }
    }

    @Test
    fun `loads load environment files in subprojects`() {
        val project = createProjectWithoutPlugin {
            withProjectDir("with-env")
        }

        createProjectWithoutPlugin {
            withParent(project)
        }

        project.apply<GradleHelperCorePlugin>()

        localEnv.forEach {(key, value) ->
            assertContains(env.toMap(), key)
            assertEquals(env[key], value)
        }
    }

}