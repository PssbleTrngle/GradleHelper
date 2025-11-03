package com.possible_triangle.gradle.access

import net.minecraftforge.gradle.mcp.tasks.GenerateSRG
import net.minecraftforge.srgutils.IMappingFile
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.getByName

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

fun Project.forgeMappings(): Remapper {
    val downloadMappings = tasks.getByName<GenerateSRG>("createMcpToSrg")

    val mappings by lazy {
        val from = downloadMappings.output.get().asFile
        logger.info("Loading MCP Names from ${from.absoluteFile}")
        IMappingFile.load(from)
    }

    return object : Remapper {
        override fun remapClass(value: String): String {
            return mappings.remapClass(value)
        }

        override fun remapField(className: String, field: String): String {
            return mappings.getClass(className).remapField(field)
        }

        override fun remapMethod(className: String, method: String, descriptor: String): String {
            return mappings.getClass(className).remapMethod(method, descriptor) +
                    mappings.remapDescriptor(descriptor)
        }

        override val task get() = downloadMappings
    }
}

fun Project.detectMappings(): Remapper {
    if (plugins.findPlugin("net.minecraftforge.gradle") != null) {
        return forgeMappings()
    }

    return Remapper.empty()
}