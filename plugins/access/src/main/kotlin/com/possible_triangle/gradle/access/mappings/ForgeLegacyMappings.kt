package com.possible_triangle.gradle.access.mappings

import com.possible_triangle.gradle.access.Remapper
import net.neoforged.moddevgradle.legacyforge.dsl.ObfuscationExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the
import net.neoforged.srgutils.IMappingFile;
import org.gradle.api.Task

fun Project.forgeLegacyMappings(): Remapper {
    val obfuscation = the<ObfuscationExtension>()
    val input = obfuscation.namedToSrgMappings

    val mappings by lazy {
        val from = input.get().asFile
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
            task.inputs.file(input)
        }
    }
}