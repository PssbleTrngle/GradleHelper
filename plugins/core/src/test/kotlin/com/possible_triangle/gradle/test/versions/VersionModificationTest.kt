package com.possible_triangle.gradle.test.versions

import com.possible_triangle.gradle.upload.addVersionMetadata
import com.possible_triangle.gradle.upload.stripVersionMetadata
import com.possible_triangle.gradle.upload.toSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals

class VersionModificationTest {
    @Test
    fun `adds version metadata`() {
        val input = "1.0.0"
        val actual = input.addVersionMetadata("something")
        val expected = "1.0.0+something"
        assertEquals(expected, actual)
    }

    @Test
    fun `adds additional version metadata`() {
        val input = "1.0.0+something"
        val actual = input.addVersionMetadata("else")
        val expected = "1.0.0+something.else"
        assertEquals(expected, actual)
    }

    @Test
    fun `does not add duplicate version metadata`() {
        val input = "1.0.0+something"
        val actual = input.addVersionMetadata("something")
        val expected = "1.0.0+something"
        assertEquals(expected, actual)
    }

    @Test
    fun `adds snapshot suffix`() {
        val input = "1.0.0"
        val actual = input.toSnapshot()
        val expected = "1.0.0-SNAPSHOT"
        assertEquals(expected, actual)
    }

    @Test
    fun `keeps suffix when adding snapshot suffix`() {
        val input = "1.0.0-rc"
        val actual = input.toSnapshot()
        val expected = "1.0.0-rc.SNAPSHOT"
        assertEquals(expected, actual)
    }

    @Test
    fun `keeps suffix when adding snapshot suffix with metadata`() {
        val input = "1.0.0-rc+metadata"
        val actual = input.toSnapshot()
        val expected = "1.0.0-rc.SNAPSHOT+metadata"
        assertEquals(expected, actual)
    }

    @Test
    fun `adds snapshot suffix before metadata`() {
        val input = "1.0.0+something.else"
        val actual = input.toSnapshot()
        val expected = "1.0.0-SNAPSHOT+something.else"
        assertEquals(expected, actual)
    }

    @Test
    fun `strips version metadata`() {
        val input = "1.0.0+something.else"
        val actual = input.stripVersionMetadata()
        val expected = "1.0.0"
        assertEquals(expected, actual)
    }

    @Test
    fun `keeps suffix when stripping version metadata`() {
        val input = "1.0.0-rc+something.else"
        val actual = input.stripVersionMetadata()
        val expected = "1.0.0-rc"
        assertEquals(expected, actual)
    }
}
