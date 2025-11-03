package com.possible_triangle.gradle.publishing

import groovy.util.Node
import groovy.util.NodeList
import org.gradle.api.publish.maven.MavenPublication

data class DependencyFilter(
    val groupId: String? = null,
    val artifactId: String? = null,
    val version: String? = null,
    val scope: String? = null
)

fun MavenPublication.removeRuntimeDependencies() {
    removePomDependencies(DependencyFilter(scope = "runtime"))
}

fun MavenPublication.removePomDependencies() {
    suppressAllPomMetadataWarnings()

    pom.withXml {
        val node = asNode()
        val list = node.get("dependencies") as NodeList
        list.forEach { node.remove(it as Node) }
    }
}

private fun Node.all(key: String) = (get(key) as List<Node>?) ?: emptyList()
private fun Node.first(key: String) = all(key).firstOrNull()

private fun matchesOrNull(filter: String?, node: Node?): Boolean {
    if (node == null || filter == null) return false
    val value = when (val it = node.value()) {
        is NodeList -> it.first()
        is String -> it
        else -> null
    }
    return value == filter
}

private fun Node.test(filter: DependencyFilter): Boolean {
    return matchesOrNull(filter.groupId, first("groupId"))
            || matchesOrNull(filter.artifactId, first("artifactId"))
            || matchesOrNull(filter.version, first("version"))
            || matchesOrNull(filter.scope, first("scope"))
}

fun MavenPublication.removePomDependencies(filter: DependencyFilter) {
    suppressAllPomMetadataWarnings()

    pom.withXml {
        val node = asNode().first("dependencies") ?: return@withXml
        val dependencies = node.all("dependency")
        dependencies.forEach {
            if (it.test(filter)) node.remove(it)
        }
    }
}