package com.possible_triangle.gradle.features.loaders

import com.possible_triangle.gradle.mod
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import java.io.File

interface WithAccessWidener {
    val project: Project

    fun accessWidener(file: Provider<File>)
    fun accessWidener(file: File) = accessWidener(project.provider { file })
    fun accessWidener(project: Project) = accessWidener(project.mod.id.map { project.file("src/main/resources/$it.accesswidener") })
    fun accessWidener() = accessWidener(project)

}

interface WithAccessTransformer {
    val project: Project

    fun accessTransformer(file: Provider<File>)
    fun accessTransformer(file: File) = accessTransformer(project.provider { file })
    fun accessTransformer(project: Project) = accessTransformer(project.file("src/main/resources/META-INF/accesstransformer.cfg"))
    fun accessTransformer() = accessTransformer(project)

}