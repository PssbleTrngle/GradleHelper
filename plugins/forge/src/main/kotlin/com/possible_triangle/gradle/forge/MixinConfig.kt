package com.possible_triangle.gradle.forge

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MixinConfig(
    val parentName: String? = null,
    val target: String? = null,
    val minVersion: String? = null,
    val refmap: String? = null,
    val refmapWrapper: String? = null,
    val plugin: String? = null,
    @SerialName("package") val mixinPackage: String? = null,
    val compatibilityLevel: String? = null,
    val required: Boolean? = null,
    val verbose: Boolean? = null,
    val setSourceFile: Boolean? = null,
    val priority: Int? = null,
    val mixinPriority: Int? = null,
    val mixins: List<String>? = null,
    val client: List<String>? = null,
    val server: List<String>? = null,
)
