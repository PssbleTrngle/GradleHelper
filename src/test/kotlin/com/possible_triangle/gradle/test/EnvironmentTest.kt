package com.possible_triangle.gradle.test

import com.possible_triangle.gradle.GradleHelperPlugin
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.env
import org.junit.jupiter.api.Test
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
        createProject {
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

        project.apply<GradleHelperPlugin>()

        localEnv.forEach {(key, value) ->
            assertContains(env.toMap(), key)
            assertEquals(env[key], value)
        }
    }

}