package com.possible_triangle.gradle.vanilla

import com.possible_triangle.gradle.features.loaders.AbstractLoaderExtension
import com.possible_triangle.gradle.features.loaders.LoaderExtension
import org.gradle.api.Project

interface CommonExtension : LoaderExtension

internal open class CommonExtensionImpl(
    project: Project,
) : AbstractLoaderExtension(project),
    CommonExtension
