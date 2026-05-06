package com.possible_triangle.gradle.publishing

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.work.DisableCachingByDefault

internal fun Project.removeRuntimeModuleDependencies() {
    tasks.named<ModifyModuleMetadataTask>(TASK_NAME) {
        removeRuntimeElements.set(true)
    }
}

internal fun Project.removeModuleDependencies() {
    tasks.withType<GenerateModuleMetadata> {
        enabled = false
    }
}

private const val TASK_NAME = "modifyMetadataFile"

@DisableCachingByDefault
abstract class ModifyModuleMetadataTask : DefaultTask() {
    @Input
    val filters = hashSetOf<DependencyFilter>()

    @get:Input
    abstract val removeRuntimeElements: Property<Boolean>
}

private val GSON: Gson = GsonBuilder().setPrettyPrinting().create()

private fun matchesOrNull(
    filter: String?,
    value: String,
): Boolean {
    if (filter == null) return false
    return value == filter
}

private data class Dependency(
    val group: String,
    val module: String,
) {
    companion object {
        fun from(json: JsonElement): Dependency {
            val group = json.asJsonObject.get("group").asString
            val module = json.asJsonObject.get("module").asString
            return Dependency(group, module)
        }
    }
}

internal fun Project.registerModuleModifyTask() {
    val task =
        tasks.register<ModifyModuleMetadataTask>(TASK_NAME) {
            removeRuntimeElements.convention(false)

            doFirst {
                logger.debug("modifying module metadata")

                inputs.files.forEach { moduleFile ->
                    val json = GSON.fromJson(moduleFile.readText(), JsonObject::class.java)
                    val variants =
                        json
                            .getAsJsonArray("variants")
                            ?.map { it.asJsonObject }
                            ?.associateBy { it.get("name").asString }
                            ?: emptyMap()

                    val apiElementDependencies =
                        variants["apiElements"]?.let { variant ->
                            variant.getAsJsonArray("dependencies")?.map { Dependency.from(it) }
                        } ?: emptyList()

                    variants.forEach { (name, variant) ->
                        val dependencies = variant.getAsJsonArray("dependencies")

                        if (name == "runtimeElements" && removeRuntimeElements.get()) {
                            dependencies?.removeAll {
                                val dependency = Dependency.from(it)
                                !apiElementDependencies.contains(dependency)
                            }
                        }

                        dependencies?.removeAll {
                            val dependency = Dependency.from(it)
                            filters.any { filter ->
                                // TODO respect version filter?
                                matchesOrNull(filter.groupId, dependency.group) ||
                                    matchesOrNull(filter.artifactId, dependency.module)
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

internal fun Project.removeModuleDependencies(filter: DependencyFilter) {
    tasks.named<ModifyModuleMetadataTask>(TASK_NAME) {
        filters.add(filter)
    }
}
