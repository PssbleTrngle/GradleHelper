package com.possible_triangle.gradle.access

import org.gradle.api.file.RegularFileProperty

interface AccessTransformerExtension {
    val from: RegularFileProperty
}