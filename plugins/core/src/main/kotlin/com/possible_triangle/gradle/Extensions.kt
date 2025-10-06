package com.possible_triangle.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.Provider
import  org.gradle.kotlin.dsl.property

inline fun <reified TInterface, reified TImplementation : TInterface> ExtensionContainer.create(name: String): TImplementation {
    return create(TInterface::class.java, name, TImplementation::class.java) as TImplementation
}

inline fun <reified T : Any> ObjectFactory.property(default: T?) = property<T>().convention(default)
inline fun <reified T : Any> ObjectFactory.property(default: Provider<out T>) = property<T>().convention(default)