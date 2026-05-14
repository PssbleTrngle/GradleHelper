package com.possible_triangle.gradle

import org.gradle.api.Task
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.register

inline fun <reified TInterface : Any, reified TImplementation : TInterface> ExtensionContainer.create(name: String): TImplementation =
    create(TInterface::class.java, name, TImplementation::class.java) as TImplementation

inline fun <reified T : Any> ObjectFactory.property(default: T?) = property<T>().convention(default)

inline fun <reified T : Any> ObjectFactory.property(default: Provider<out T>) = property<T>().convention(default)

inline fun <reified T : Task> TaskContainer.getOrCreate(name: String): T = findByName(name) as T? ?: register<T>(name).get()
