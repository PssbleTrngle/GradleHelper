package com.possible_triangle.gradle.access

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

interface AccessTransformerExtension {
    val from: RegularFileProperty
}