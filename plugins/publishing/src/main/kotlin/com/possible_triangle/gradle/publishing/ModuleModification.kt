package com.possible_triangle.gradle.publishing

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonElement
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.work.DisableCachingByDefault

internal fun Project.removeRuntimeModuleDependencies() {
    removeModuleDependencies(DependencyFilter(scope = "runtime"))
}

internal fun Project.removeModuleDependencies() {
    tasks.withType<GenerateModuleMetadata> {
        enabled = false
    }
}

private val TASK_NAME = "modifyMetadataFile"

@DisableCachingByDefault
abstract class ModifyModuleMetadataTask : DefaultTask() {
    @Input
    val filters = hashSetOf<DependencyFilter>()
}

private val GSON: Gson = GsonBuilder().setPrettyPrinting().create()

private fun matchesOrNull(filter: String?, element: JsonElement?): Boolean {
    if (element == null || filter == null) return false
    val value = element.asString
    return value == filter
}

fun Project.registerModuleModifyTask() {
    val task = tasks.register<ModifyModuleMetadataTask>(TASK_NAME) {
        doFirst {
            logger.debug("modifying module metadata")

            inputs.files.forEach { moduleFile ->
                val json = GSON.fromJson(moduleFile.readText(), JsonObject::class.java)
                val variants = json.getAsJsonArray("variants")

                variants?.removeAll { variant ->
                    filters
                        .mapNotNull { it.scope }
                        .map { "${it}Elements" }
                        .any { it == variant.asJsonObject.get("name").asString }
                }

                variants?.forEach { variant ->
                    val dependencies = variant.asJsonObject.getAsJsonArray("dependencies")
                    dependencies?.removeAll {
                        val dependency = it.asJsonObject
                        filters.any { filter ->
                            // TODO respect version  filter?
                            matchesOrNull(filter.groupId, dependency.get("group"))
                                    || matchesOrNull(filter.artifactId, dependency.get("module"))
                        }
                    }
                }

                moduleFile.writeText(GSON.toJson(json))
            }
        }
    }

    tasks.withType<GenerateModuleMetadata> {
        finalizedBy(task)
        task.configure {
            inputs.file(outputFile)
        }
    }
}

fun Project.removeModuleDependencies(filter: DependencyFilter) {
    tasks.named<ModifyModuleMetadataTask>(TASK_NAME) {
        filters.add(filter)
    }
}