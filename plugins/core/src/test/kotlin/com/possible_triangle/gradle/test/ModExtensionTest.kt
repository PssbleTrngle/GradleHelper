package com.possible_triangle.gradle.test

import com.possible_triangle.gradle.GradleHelperCorePlugin
import com.possible_triangle.gradle.mod
import org.gradle.api.internal.provider.MissingValueException
import org.gradle.kotlin.dsl.extra
import org.junit.Assert.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModExtensionTest {

    @Test
    fun `can customize mod values`() {
        val project = createProject<GradleHelperCorePlugin>()

        project.mod {
            id.set("custom-id")
            name.set("custom-name")
            author.set("custom-author")
        }

        assertEquals("custom-id", project.mod.id.get())
        assertEquals("custom-name", project.mod.name.get())
        assertEquals("custom-author", project.mod.author.get())
    }

    @Test
    fun `cannot read unset value`() {
        val project = createProject<GradleHelperCorePlugin>()

        project.mod {
            id.set("custom-id")
        }

        assertThrows(MissingValueException::class.java) {
            project.mod.name.get()
        }
    }

    @Test
    fun `uses extra properties as default`() {
        val project = createProject<GradleHelperCorePlugin> {
            withProjectDir("example")
        }

        assertTrue(project.extra.properties.size >= 4)
        assertEquals("example-mod", project.mod.id.get())
        assertEquals("Example Name", project.mod.name.get())
        assertEquals("Example Author", project.mod.author.get())
        assertEquals("1.2.3", project.mod.version.get())
    }

}