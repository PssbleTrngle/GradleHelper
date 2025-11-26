package com.possible_triangle.gradle.fabric

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.apache.tools.ant.filters.BaseFilterReader
import java.io.File
import java.io.Reader

private inline fun <reified T : JsonElement> JsonObject.getOrAdd(property: String, default: T): T {
    return if (has(property)) get(property) as T
    else default.also {
        add(property, it)
    }
}

class AddInterfaceInjections(input: Reader) : BaseFilterReader(input) {

    private lateinit var out: String
    private lateinit var from: File
    private var index = 0

    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    fun setFrom(file: File) {
        this.from = file
    }

    private fun initialize() {
        if (this::out.isInitialized) return

        val injections = gson.fromJson(from.readText(), JsonObject::class.java)

        val json = gson.fromJson(readFully(), JsonObject::class.java)

        val data = json
            .getOrAdd("custom", JsonObject())
            .getOrAdd("loom:injected_interfaces", JsonObject())

        injections.entrySet().forEach { (key, element) ->
            data.add(key, element)
        }

        out = gson.toJson(json)
    }

    override fun read(): Int {
        initialize()
        if (index >= out.length) return -1
        return out[index++].toInt()
    }

}