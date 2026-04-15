package com.possible_triangle.gradle.access

import com.possible_triangle.gradle.access.mappings.forgeGradleMappings
import com.possible_triangle.gradle.access.mappings.forgeLegacyMappings
import org.gradle.api.Project
import org.gradle.api.Task

interface Remapper {
    fun remapClass(value: String): String
    fun remapField(className: String, field: String): String
    fun remapMethod(className: String, method: String, descriptor: String): String

    fun configureTask(task: Task) {
    }

    companion object {
        fun empty(block: Task.() -> Unit = {}) = object : Remapper {
            override fun remapClass(value: String) = value
            override fun remapField(className: String, field: String) = field

            override fun remapMethod(
                className: String,
                method: String,
                descriptor: String
            ) = method + descriptor

            override fun configureTask(task: Task) = task.block()
        }
    }
}

fun Project.detectMappings(): Remapper {
    if (plugins.findPlugin("net.minecraftforge.gradle") != null) {
        return forgeGradleMappings()
    }

    if (plugins.findPlugin("net.neoforged.moddev.legacyforge") != null) {
        return forgeLegacyMappings()
    }

    return Remapper.empty()
}