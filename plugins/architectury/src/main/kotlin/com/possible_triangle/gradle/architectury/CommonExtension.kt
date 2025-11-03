package com.possible_triangle.gradle.architectury

import com.possible_triangle.gradle.features.loaders.AbstractLoaderExtension
import com.possible_triangle.gradle.features.loaders.LoaderExtension
import com.possible_triangle.gradle.features.loaders.WithAccessWidener
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.the
import java.io.File

interface CommonExtension : LoaderExtension, WithAccessWidener

internal open class CommonExtensionImpl(override val project: Project) : AbstractLoaderExtension(), CommonExtension {

    override fun accessWidener(file: Provider<File>) {
        project.the<LoomGradleExtensionAPI>().accessWidenerPath.set { file.get() }
    }

}