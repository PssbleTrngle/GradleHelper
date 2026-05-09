package com.possible_triangle.gradle.test

import com.possible_triangle.gradle.javaVersionFor
import kotlin.test.Test
import kotlin.test.assertEquals

class JavaVersionTest {
    @Test
    fun `correctly detects java version`() {
        versionsProvider().forEach { (minecraftVersion, expected) ->
            val actual = javaVersionFor(minecraftVersion)
            assertEquals(expected, actual)
        }
    }

    companion object {
        fun versionsProvider() =
            sequenceOf(
                "1.18.2" to 17,
                "1.18.3" to 17,
                "1.18.5" to 17,
                "1.19.2" to 17,
                "1.20.1" to 17,
                "1.21" to 21,
                "1.21.1" to 21,
                "1.21.8" to 21,
                "26.1" to 26,
                "26.1.1" to 26,
                "26.1.2" to 26,
            )
    }
}
