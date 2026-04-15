package com.possible_triangle.gradle.access.mappings

import com.possible_triangle.gradle.access.Remapper
import net.minecraftforge.gradle.mcp.tasks.GenerateSRG
import net.minecraftforge.srgutils.IMappingFile
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.getByName

fun Project.forgeGradleMappings(): Remapper {
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

        override fun configureTask(task: Task) {
            task.dependsOn(downloadMappings)
        }
    }
}