package com.possible_triangle.gradle.access

import java.io.File

internal fun String.trimComments(): String {
    if (!contains('#')) return this
    return substring(0, indexOf('#'))
}

internal fun parseEntry(statements: List<String>): AccessWidener.Entry {
    val modifier = AccessWidener.Modifier.valueOf(statements[0].uppercase())
    val target = AccessWidener.Target.valueOf(statements[1].uppercase())
    val className = statements[2]

    return when (target) {
        AccessWidener.Target.CLASS -> AccessWidener.ClassEntry(modifier, className)
        AccessWidener.Target.METHOD -> AccessWidener.MethodEntry(modifier, className, statements[3], statements[4])
        AccessWidener.Target.FIELD -> AccessWidener.FieldEntry(modifier, className, statements[3], statements[4])
    }
}

fun parseAccessWidener(file: File): AccessWidener {
    if (!file.exists()) error("unable to find access widener file '${file}'")
    val lines = file.readLines()
        .map { it.trimComments() }
        .map { it.trim() }
        .filterNot { it.isEmpty() }
        .map { it.split("\\s+".toRegex()) }

    val (header, version) = lines.first()

    if (header != "accessWidener") error("invalid header '${header}'")
    if (version != "v1") error("access widener transformation does only support v1")

    val entries = lines.subList(1, lines.size).map(::parseEntry)

    return AccessWidener(entries)
}