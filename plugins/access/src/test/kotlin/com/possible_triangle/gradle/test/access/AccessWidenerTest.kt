package com.possible_triangle.gradle.test.access

import com.possible_triangle.gradle.access.parseAccessWidener
import com.possible_triangle.gradle.access.toAccessTransformer
import com.possible_triangle.gradle.test.createProjectWithoutPlugin
import java.io.File
import kotlin.test.Test
import kotlin.test.assertNotNull

class AccessWidenerTest {

    @Test
    fun `parses correctly`() {
        val project = createProjectWithoutPlugin {
            withProjectDir(File("src/test/resources"))
        }

        val parsed = parseAccessWidener(project.file("aw/simple.accesswidener"))

        assertNotNull(parsed)
    }

    @Test
    fun `transforms correctly`() {
        val project = createProjectWithoutPlugin {
            withProjectDir(File("src/test/resources"))
        }

        val parsed = parseAccessWidener(project.file("aw/simple.accesswidener"))
        val transformed = parsed.toAccessTransformer()

        assertNotNull(transformed)
    }

}