buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
        classpath("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:4.2.1.3168")
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    //apply(plugin = "org.jetbrains.kotlin.jvm")

    //java {
    //    toolchain {
    //        languageVersion.set(JavaLanguageVersion.of(17))
    //    }
    //    withSourcesJar()
    //}
}