package com.possible_triangle.gradle.test

import org.gradle.kotlin.dsl.uploadToCurseforge
import org.gradle.kotlin.dsl.uploadToModrinth
import org.junit.jupiter.api.Test

class UploadTest {

    @Test
    fun `can configure cursegradle without token`() {
        val project = createProject {
            withProjectDir("example")
        }

        project.uploadToCurseforge()
    }

    @Test
    fun `can configure modrinth without token`() {
        val project = createProject {
            withProjectDir("example")
        }

        project.uploadToModrinth()
    }

}