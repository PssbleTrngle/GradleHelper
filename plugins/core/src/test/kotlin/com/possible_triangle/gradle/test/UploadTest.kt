package com.possible_triangle.gradle.test

import com.possible_triangle.gradle.GradleHelperCorePlugin
import com.possible_triangle.gradle.upload.UploadExtension
import org.gradle.kotlin.dsl.the
import kotlin.test.Test

class UploadTest {

    @Test
    fun `can configure cursegradle without token`() {
        val project = createProject<GradleHelperCorePlugin> {
            withProjectDir("example")
        }

        project.the<UploadExtension>().curseforge {
            projectId.set("test-id")
        }
    }

    @Test
    fun `can configure modrinth without token`() {
        val project = createProject<GradleHelperCorePlugin> {
            withProjectDir("example")
        }


        project.the<UploadExtension>().modrinth {
            projectId.set("test-id")
        }
    }

}