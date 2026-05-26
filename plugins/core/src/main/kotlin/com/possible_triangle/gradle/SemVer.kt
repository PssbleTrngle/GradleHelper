package com.possible_triangle.gradle

data class SemVer(
    val major: Int,
    val minor: Int,
    val patch: Int?,
) {
    companion object {
        fun parse(value: String): SemVer {
            val parts = value.split(".").map { it.toInt() }
            if (parts.size < 2) error("illegal version format '$value'")
            return SemVer(
                major = parts[0],
                minor = parts[1],
                patch = parts.getOrNull(2),
            )
        }
    }
}
