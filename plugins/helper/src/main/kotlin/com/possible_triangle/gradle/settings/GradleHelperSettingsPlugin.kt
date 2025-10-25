package com.possible_triangle.gradle.settings

import org.apache.log4j.LogManager
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import java.net.URI

@Suppress("unused")
class GradleHelperSettingsPlugin : Plugin<Settings> {

    private val logger = LogManager.getLogger(GradleHelperSettingsPlugin::class.java)

    override fun apply(target: Settings) {
        logger.info("using gradle helper version ${BuildParameters.MAJOR_VERSION}")

        target.pluginManagement {
            repositories {
                maven {
                    url = URI("https://registry.somethingcatchy.net/repository/maven-public/")
                }
            }

            resolutionStrategy {
                eachPlugin {
                    if (requested.version == null && requested.id.namespace == "com.possible-triangle") {
                        val snapshotVersion = "${BuildParameters.MAJOR_VERSION}.+"
                        logger.info("resolving $snapshotVersion for ${requested.id.name}")
                        useVersion(snapshotVersion)
                    }
                }
            }
        }
    }

}