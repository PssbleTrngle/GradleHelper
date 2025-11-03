package com.possible_triangle.gradle.access

/**
 * credits to [isXander/modstitch](https://github.com/isXander/modstitch/blob/master/src/main/kotlin/dev/isxander/modstitch/util/AccessWidener.kt)
 */
data class AccessWidener(
    val entries: List<Entry>,
) {

    sealed interface Entry {
        val target: Target
        val modifier: Modifier
        val className: String
    }

    data class ClassEntry(
        override val modifier: Modifier,
        override val className: String,
    ) : Entry {
        override val target = Target.CLASS
    }

    data class MethodEntry(
        override val modifier: Modifier,
        override val className: String,
        val name: String,
        val descriptor: String,
    ) : Entry {
        override val target = Target.METHOD
    }

    data class FieldEntry(
        override val modifier: Modifier,
        override val className: String,
        val name: String,
        val descriptor: String,
    ) : Entry {
        override val target = Target.FIELD
    }

    enum class Modifier {
        ACCESSIBLE,
        MUTABLE,
        EXTENDABLE,
    }

    enum class Target {
        CLASS,
        METHOD,
        FIELD,
    }

}
