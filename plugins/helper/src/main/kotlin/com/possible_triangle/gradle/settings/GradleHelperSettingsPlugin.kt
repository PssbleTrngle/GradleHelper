package com.possible_triangle.gradle.settings

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import java.net.URI

@Suppress("unused")
class GradleHelperSettingsPlugin : Plugin<Settings> {

    override fun apply(target: Settings) {
        target.pluginManagement {
            repositories {
                maven {
                    url = URI("https://registry.somethingcatchy.net/repository/maven-public/")
                }
            }

            resolutionStrategy {
                eachPlugin {
                    if (requested.id.namespace == "com.possible-triangle") {
                        useVersion(BuildParameters.VERSION)
                    }
                }
            }
        }
    }

}