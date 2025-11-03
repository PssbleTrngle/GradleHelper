package com.possible_triangle.gradle.access

internal fun AccessWidener.Modifier.transform(): String {
    return when (this) {
        AccessWidener.Modifier.ACCESSIBLE -> "public"
        AccessWidener.Modifier.MUTABLE -> "public-f"
        AccessWidener.Modifier.EXTENDABLE -> "protected-f"
    }
}

fun String.remapNotation() = replace('/', '.')

fun AccessWidener.toAccessTransformer(remapper: Remapper = Remapper.empty()): String {
    val transformed = entries.map {
        val modifier = it.modifier.transform()
        val className = remapper.remapClass(it.className).remapNotation()

        val additional = when (it) {
            is AccessWidener.ClassEntry -> emptyList()
            is AccessWidener.FieldEntry -> listOf(remapper.remapField(it.className, it.name), "# ${it.name}")
            is AccessWidener.MethodEntry -> listOf(remapper.remapMethod(it.className, it.name, it.descriptor), "# ${it.name}")
        }

        listOf(modifier, className) + additional
    }

    val lines = transformed.map { it.joinToString(" ") }
    return lines.joinToString("\n")
}