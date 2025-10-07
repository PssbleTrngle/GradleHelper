package com.possible_triangle.gradle.architectury

import com.possible_triangle.gradle.features.loaders.AbstractLoaderExtension
import com.possible_triangle.gradle.features.loaders.LoaderExtension

interface CommonExtension : LoaderExtension

internal open class CommonExtensionImpl() : AbstractLoaderExtension(), CommonExtension