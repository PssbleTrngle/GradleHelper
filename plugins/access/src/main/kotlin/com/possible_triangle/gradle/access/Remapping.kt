package com.possible_triangle.gradle.access

import org.gradle.api.Project
import org.gradle.api.Task

interface Remapper {
    fun remapClass(value: String): String
    fun remapField(className: String, field: String): String
    fun remapMethod(className: String, method: String, descriptor: String): String
    val task: Task?

    companion object {
        fun empty(task: Task? = null) = object : Remapper {
            override fun remapClass(value: String) = value
            override fun remapField(className: String, field: String) = field

            override fun remapMethod(
                className: String,
                method: String,
                descriptor: String
            ) = method + descriptor

            override val task = task
        }
    }
}



fun Project.detectMappings(): Remapper {
    if (plugins.findPlugin("net.minecraftforge.renamer") != null) {
        error("ForgeGradle 7.0 is not supported yet")
    }

    return Remapper.empty()
}