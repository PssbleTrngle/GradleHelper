package com.possible_triangle.gradle.forge

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import org.apache.tools.ant.filters.BaseFilterReader
import java.io.Reader

class AddMixinRefmap(input: Reader) : BaseFilterReader(input) {

    val gson = GsonBuilder().setPrettyPrinting().create()

    private lateinit var out: String
    private lateinit var name: String
    private var index = 0

    fun setName(name: String) {
        this.name = name
    }

    private fun initialize() {
        if (this::out.isInitialized) return
        val config = gson.fromJson(readFully(), JsonObject::class.java)

        if (!config.has("refmap")) {
            config.addProperty("refmap", name)
        }

        out = gson.toJson(config)
    }

    override fun read(): Int {
        initialize()
        if (index >= out.length) return -1
        return out[index++].toInt()
    }

}