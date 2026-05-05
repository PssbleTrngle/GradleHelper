package com.possible_triangle.gradle.access.mappings

import com.possible_triangle.gradle.access.Remapper
import net.neoforged.moddevgradle.legacyforge.dsl.LegacyForgeExtension
import net.neoforged.nfrtgradle.NeoFormRuntimeTask
import net.neoforged.srgutils.IMappingFile
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Implements its own caching")
abstract class NamedToIntermediaryMapping : NeoFormRuntimeTask() {
    @OutputFile
    abstract fun getOutput(): RegularFileProperty

    @Input
    abstract fun getForgeVersion(): Property<String>

    @TaskAction
    fun createMappings() {
        val args =
            listOf(
                "run",
                "--neoforge",
                getForgeVersion().get(),
                "--dist",
                "joined",
                "--repository",
                "https://maven.minecraftforge.net/",
                "--write-result",
                "namedToIntermediaryMapping:${getOutput().get().asFile.absoluteFile}",
            )

        run(args)
    }
}

fun Project.forgeLegacyMappings(): Remapper {
    val mappingsFile = layout.buildDirectory.file("generated/namedToIntermediaryMapping.tsrg")

    val mappings by lazy {
        val from = mappingsFile.get().asFile
        logger.info("Loading MCP Names from ${from.absoluteFile}")
        IMappingFile.load(from)
    }

    val extension = the<LegacyForgeExtension>()

    val mappingsTask =
        tasks.register<NamedToIntermediaryMapping>("createNamedToIntermediaryMapping") {
            getOutput().set(mappingsFile)
            getForgeVersion().set("net.minecraftforge:forge:${extension.version}:userdev")
        }

    return object : Remapper {
        override fun remapClass(value: String): String = mappings.remapClass(value)

        override fun remapField(
            className: String,
            field: String,
        ): String = mappings.getClass(className).remapField(field)

        override fun remapMethod(
            className: String,
            method: String,
            descriptor: String,
        ): String =
            mappings.getClass(className).remapMethod(method, descriptor) +
                mappings.remapDescriptor(descriptor)

        override fun configureTask(task: Task) {
            task.dependsOn(mappingsTask)
        }
    }
}
