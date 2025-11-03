package com.possible_triangle.gradle.access

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated
import java.io.File

private const val TRANSFORM_TASK = "transformAccessWidener"

private fun Project.generatedAccessTransformer() =
    layout.buildDirectory.file("generated/accesstransformer.cfg").map {
        val file = it.asFile
        file.ensureParentDirsCreated()
        if (!file.exists()) file.createNewFile()
        file
    }

fun Project.generateAccessTransformer(from: Provider<File>): Provider<File> {
    val output = generatedAccessTransformer()

    val transformAccessWidener = tasks.register(TRANSFORM_TASK) {
        val remapper = detectMappings()
        remapper.task?.let { dependsOn(it) }
        outputs.file(output)
        inputs.file(from)

        doLast {
            val accessWidener = parseAccessWidener(from.get())
            val transformed = accessWidener.toAccessTransformer(remapper)
            output.get().writeText(transformed)
        }
    }

    @Suppress("UnstableApiUsage")
    tasks.withType<ProcessResources> {
        dependsOn(transformAccessWidener)
        from(transformAccessWidener) {
            rename { "accesstransformer.cfg" }
            into("META-INF")
        }
    }

    return output
}

@Suppress("unused")
class AccessWidenerTransformationPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create<AccessTransformerExtension>("access")
        target.generateAccessTransformer(extension.from.map { it.asFile })
    }

}