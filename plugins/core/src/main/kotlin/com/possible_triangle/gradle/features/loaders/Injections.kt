package com.possible_triangle.gradle.features.loaders

import com.possible_triangle.gradle.coreProject
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import java.io.File

interface WithInterfaceInjections {
    val project: Project

    fun injectInterfaces(file: Provider<File>)
    fun injectInterfaces(file: File) = injectInterfaces(project.provider { file })
    fun injectInterfaces(project: Project) = injectInterfaces(project.file("interfaces.json"))
    fun injectInterfaces() = injectInterfaces(project.coreProject)
}