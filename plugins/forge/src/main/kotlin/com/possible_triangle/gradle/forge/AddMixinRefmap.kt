package com.possible_triangle.gradle.forge

import kotlinx.serialization.json.Json
import org.apache.tools.ant.filters.BaseFilterReader
import java.io.Reader

class AddMixinRefmap(input: Reader) : BaseFilterReader(input) {

    private lateinit var out: String
    private lateinit var name: String
    private var index = 0

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    fun setName(name: String) {
        this.name = name
    }

    private fun initialize() {
        if (this::out.isInitialized) return
        val config = json.decodeFromString<MixinConfig>(readFully())
        val modified = config.copy(refmap = config.refmap ?: name)
        out = json.encodeToString(modified)
    }

    override fun read(): Int {
        initialize()
        if (index >= out.length) return -1
        return out[index++].toInt()
    }

}