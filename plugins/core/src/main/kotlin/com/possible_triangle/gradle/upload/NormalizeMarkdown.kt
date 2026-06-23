package com.possible_triangle.gradle.upload

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault
abstract class NormalizeMarkdown : DefaultTask() {
    @get:Input
    abstract val from: Property<String>

    @get:OutputFile
    abstract val to: RegularFileProperty

    @get:Input
    abstract val baseUrl: Property<String>

    private fun replaceLink(result: MatchResult): String {
        val prefix = result.groups[1] ?: error("invalid regex")
        val link = result.groups[2] ?: error("invalid regex")
        return if (link.value.contains("://")) {
            result.value
        } else {
            prefix.value + baseUrl.get() + link.value
        }
    }

    @TaskAction
    fun normalize() {
        val content = from.get()

        val replaced =
            content
                .replace("(\\[.*?]\\()(.+?\\))".toRegex(), this::replaceLink)
                .replace("(src=\")(.+?\")".toRegex(), this::replaceLink)

        to.get().asFile.writeText(replaced)
    }
}
