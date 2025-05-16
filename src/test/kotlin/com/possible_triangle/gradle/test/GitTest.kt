package com.possible_triangle.gradle.test

import com.possible_triangle.gradle.features.GitExtension
import com.possible_triangle.gradle.features.enableSpotless
import org.gradle.kotlin.dsl.the
import org.junit.jupiter.api.Test
import kotlin.test.assertContains

class GitTest {

    @Test
    fun `registers git hook with spotless`() {
        val project = createProject {
            withProjectDir("example")
        }

        project.enableSpotless(true)

        val git = project.the<GitExtension>()

        assertContains(git.preCommitTasks, "spotlessApply")
    }

}